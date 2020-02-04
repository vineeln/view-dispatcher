package viewdispatcher

import grails.config.Settings
import org.grails.web.servlet.mvc.GrailsDispatcherServlet
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.util.ClassUtils

import grails.plugins.*

class ViewDispatcherGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.11 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "View Dispatcher" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/view-dispatcher"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    def loadAfter = ['controllers']

    Closure doWithSpring() { {->
            // TODO Implement runtime spring config (optional)
    def application = grailsApplication
    def config = application.config

    boolean isTomcat = ClassUtils.isPresent("org.apache.catalina.startup.Tomcat", application.classLoader)
    String grailsServletPath = config.getProperty(Settings.WEB_SERVLET_PATH, isTomcat ? Settings.DEFAULT_TOMCAT_SERVLET_PATH : Settings.DEFAULT_WEB_SERVLET_PATH)

    // add the dispatcher servlet
    dispatcherServlet(ViewDispatcherServlet)
    dispatcherServletRegistration(ServletRegistrationBean, ref("dispatcherServlet"), grailsServletPath) {
        loadOnStartup = 2
        asyncSupported = true
        multipartConfig = multipartConfigElement
    }
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
