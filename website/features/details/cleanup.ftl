<#import "../../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <div class="header-group">
            <h1>@Cleanup</h1>

            <h3>Automatic resource management: Call your <code>close()</code> methods safely with no hassle.</h3>
        </div>
    </div>
    <div class="row">
        <div class="overview">
            <h3>Overview</h3>

            <p>
                You can use <code>@Cleanup</code> to ensure a given resource is automatically cleaned up before the code
                execution path exits your
                current scope. You do this by annotating any local variable declaration with the <code>@Cleanup</code>
                annotation like so:<br/>
                <pre>@Cleanup InputStream in = new FileInputStream("some/file");</pre>

                As a result, at the end of the scope you're in, <code>in.close()</code> is called. This call is
                guaranteed to run by way of a
                try/finally construct. Look at the example below to see how this works.
            </p>

            <p>
                If the type of object you'd like to cleanup does not have a <code>close()</code> method, but some other
                no-argument method, you can
                specify the name of this method like so:<br/>
                <code>@Cleanup("dispose") org.eclipse.swt.widgets.CoolBar bar = new CoolBar(parent, 0);</code><br/>
                By default, the cleanup method is presumed to be <code>close()</code>. A cleanup method that takes 1 or
                more arguments cannot be called via
                <code>@Cleanup</code>.
            </p>
        </div>
    </div>
    <@u.comparison />
    <div class="row">
        <div class="overview confKeys">
            <h3>Supported configuration keys:</h3>
            <dl>
                <dt><code>lombok.cleanup.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default: not
                    set)
                </dt>
                <dd>Lombok will flag any usage of <code>@Cleanup</code> as a warning or error if configured.</dd>
            </dl>
        </div>
        <div class="row">
            <h3>Small print</h3>

            <div class="smallprint">
                <p>
                    In the finally block, the cleanup method is only called if the given resource is not
                    <code>null</code>. However, if you use <code>delombok</code>
                    on the code, a call to <code>lombok.Lombok.preventNullAnalysis(Object o)</code> is inserted to
                    prevent warnings if static code analysis could
                    determine that a null-check would not be needed. Compilation with <code>lombok.jar</code> on the
                    classpath removes that method call,
                    so there is no runtime dependency.
                </p>

                <p>
                    If your code throws an exception, and the cleanup method call that is then triggered also throws an
                    exception, then the original exception
                    is hidden by the exception thrown by the cleanup call. You should <em>not</em> rely on this
                    'feature'. Preferably, lombok would like to generate
                    code so that, if the main body has thrown an exception, any exception thrown by the close call is
                    silently swallowed (but if the main body
                    exited in any other way, exceptions by the close call will not be swallowed). The authors of lombok
                    do not currently know of a feasible way
                    to implement this scheme, but if java updates allow it, or we find a way, we'll fix it.
                </p>

                <p>
                    You do still need to handle any exception that the cleanup method can generate!
                </p>
            </div>
        </div>
    </div>
</div>
</@u.page>

