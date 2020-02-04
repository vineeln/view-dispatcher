package viewdispatcher;

import org.grails.web.servlet.mvc.GrailsDispatcherServlet
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.async.WebAsyncManager
import org.springframework.web.context.request.async.WebAsyncUtils
import org.springframework.web.servlet.HandlerAdapter
import org.springframework.web.servlet.HandlerExecutionChain
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.ModelAndViewDefiningException
import org.springframework.web.util.NestedServletException
import org.springframework.web.util.WebUtils

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ViewDispatcherServlet extends GrailsDispatcherServlet {
    ViewDispatcherServlet() {
    }

    ViewDispatcherServlet(WebApplicationContext webApplicationContext) {
        super(webApplicationContext)
    }

    @Override
    protected GrailsWebRequest buildGrailsWebRequest(HttpServletRequest request, HttpServletResponse response) {
        return super.buildGrailsWebRequest(request,response)
    }

    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpServletRequest processedRequest = request;
        HandlerExecutionChain mappedHandler = null;
        boolean multipartRequestParsed = false;

        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

        try {
            ModelAndView mv = null;
            Exception dispatchException = null;

            try {
                processedRequest = checkMultipart(request);
                multipartRequestParsed = (processedRequest != request);

                // Determine handler for the current request.
                mappedHandler = getHandler(processedRequest);
                if (mappedHandler == null || mappedHandler.getHandler() == null) {
                    noHandlerFound(processedRequest, response);
                    return;
                }

                // Determine handler adapter for the current request.
                HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

                // Process last-modified header, if supported by the handler.
                String method = request.getMethod();
                boolean isGet = "GET".equals(method);
                if (isGet || "HEAD".equals(method)) {
                    long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Last-Modified value for [" + getRequestUri(request) + "] is: " + lastModified);
                    }
                    if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
                        return;
                    }
                }

                if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                    return;
                }

                // Actually invoke the handler.
                // process controller
                mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

                if (asyncManager.isConcurrentHandlingStarted()) {
                    return;
                }

                applyDefaultViewName(processedRequest, mv);
                mappedHandler.applyPostHandle(processedRequest, response, mv);

                // VRN: process the view, no exception while processing controller
                processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
            }
            catch (Exception ex) {
                dispatchException = ex;

                // VRN: handle exception which occurred during either 1. controller execution or 2. view execution
                processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
            }
            catch (Throwable err) {
                // As of 4.3, we're processing Errors thrown from handler methods as well,
                // making them available for @ExceptionHandler methods and other scenarios.
                dispatchException = new NestedServletException("Handler dispatch failed", err);

                // VRN: handle exception which occurred during either 1. controller execution or 2. view execution
                processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
            }

            // VRN: commented the following & relocated from here to within "try/catch" above.
            // VRN: so that the exceptions during view processing are also handled
            // processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
        }
        catch (Exception ex) {
            triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
        }
        catch (Throwable err) {
            triggerAfterCompletion(processedRequest, response, mappedHandler,
                    new NestedServletException("Handler processing failed", err));
        }
        finally {
            if (asyncManager.isConcurrentHandlingStarted()) {
                // Instead of postHandle and afterCompletion
                if (mappedHandler != null) {
                    mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
                }
            }
            else {
                // Clean up any resources used by a multipart request.
                if (multipartRequestParsed) {
                    cleanupMultipart(processedRequest);
                }
            }
        }
    }

    private void applyDefaultViewName(HttpServletRequest request, ModelAndView mv) throws Exception {
        if (mv != null && !mv.hasView()) {
            mv.setViewName(getDefaultViewName(request));
        }
    }

    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
                                       HandlerExecutionChain mappedHandler, ModelAndView mv, Exception exception) throws Exception {

        boolean errorView = false;

        if (exception != null) {
            if (exception instanceof ModelAndViewDefiningException) {
                logger.debug("ModelAndViewDefiningException encountered", exception);
                mv = ((ModelAndViewDefiningException) exception).getModelAndView();
            }
            else {
                Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
                mv = processHandlerException(request, response, handler, exception);
                errorView = (mv != null);
            }
        }

        // Did the handler return a view to render?
        if (mv != null && !mv.wasCleared()) {
            render(mv, request, response);
            if (errorView) {
                WebUtils.clearErrorRequestAttributes(request);
            }
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Null ModelAndView returned to DispatcherServlet with name '" + getServletName() +
                        "': assuming HandlerAdapter completed request handling");
            }
        }

        if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
            // Concurrent handling started during a forward
            return;
        }

        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(request, response, null);
        }
    }

    private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response,
                                        HandlerExecutionChain mappedHandler, Exception ex) throws Exception {

        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(request, response, ex);
        }
        throw ex;
    }
}

