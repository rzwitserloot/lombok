<#import "../../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <div class="header-group">
            <h1>@EqualsAndHashCode</h1>

            <h3>Equality made easy: Generates <code>hashCode</code> and <code>equals</code>
                implementations from the fields of your object.
            </h3>
        </div>
    </div>
    <div class="row">
        <div class="overview">
            <h3>Overview</h3>

            <p>
                Any class definition may be annotated with <code>@EqualsAndHashCode</code> to let lombok generate
                implementations of the <code>equals(Object other)</code> and <code>hashCode()</code> methods. By
                default, it'll use all non-static, non-transient fields, but you can exclude more fields by naming them
                in the optional <code>exclude</code> parameter to the annotation. Alternatively, you can specify exactly
                which fields you wish to be used by naming them in the <code>of</code> parameter.
            </p>

            <p>
                By setting <code>callSuper</code> to <em>true</em>, you can include the <code>equals</code> and
                <code>hashCode</code>
                methods of your superclass in the generated methods. For <code>hashCode</code>, the result of <code>super.hashCode()</code>
                is included in the hash algorithm, and for <code>equals</code>, the generated method will return false
                if the super implementation thinks it is not equal to the passed in object. Be aware that not all
                <code>equals</code>
                implementations handle this situation properly. However, lombok-generated <code>equals</code>
                implementations <strong>do</strong> handle this situation properly, so you can safely call your
                superclass equals if it, too, has a lombok-generated <code>equals</code> method.<br/>
            </p>

            <p>
                Setting <code>callSuper</code> to <em>true</em> when you don't extend anything (you extend <code>java.lang.Object</code>)
                is a compile-time error, because it would turn the generated <code>equals()</code> and
                <code>hashCode()</code> implementations into having the same behaviour as simply inheriting these
                methods from <code>java.lang.Object</code>: only the same object will be equal to each other and will
                have the same hashCode. Not setting <code>callSuper</code> to <em>true</em> when you extend another
                class generates a warning, because unless the superclass has no (equality-important) fields, lombok
                cannot generate an implementation for you that takes into account the fields declared by your
                superclasses. You'll need to write your own implementations, or rely on the
                <code>callSuper</code> chaining facility.
            </p>

            <p>
                <em>NEW in Lombok 0.10: </em>Unless your class is <code>final</code> and extends
                <code>java.lang.Object</code>, lombok generates a <code>canEqual</code> method
                which means JPA proxies can still be equal to their base class, but subclasses that add new state don't
                break the equals contract. The complicated reasons for
                why such a method is necessary are explained in this paper: <a
                    href="http://www.artima.com/lejava/articles/equality.html">How to Write an Equality Method in
                Java</a>.
                If all classes in a hierarchy are a mix of scala case classes and classes with lombok-generated equals
                methods, all equality will 'just work'.
                If you need to write your own equals methods, you should always override <code>canEqual</code> if you
                change <code>equals</code> and <code>hashCode</code>.
            </p>

            <p>
                <em>NEW in Lombok 1.14.0: </em>To put annotations on the <code>other</code> parameter of the <code>equals</code>
                (and, if relevant, <code>canEqual</code>) method, you can use
                <code>onParam=@__({@AnnotationsHere})</code>. Be careful though! This is an experimental feature. For
                more details see the documentation on the <a href="/features/details/on-x.html">onX</a> feature.

        </div>
        <@u.comparison />
        <div class="row">
            <h3>Supported configuration keys:</h3>
            <dl>
                <dt><code>lombok.equalsAndHashCode.doNotUseGetters</code> = [<code>true</code> | <code>false</code>]
                    (default: false)
                </dt>
                <dd>If set to <code>true</code>, lombok will access fields directly instead of using getters (if
                    available) when generating <code>equals</code> and <code>hashCode</code> methods. The annotation
                    parameter '<code>doNotUseGetters</code>', if explicitly specified, takes precedence over this
                    setting.
                </dd>
                <dt><code>lombok.equalsAndHashCode.flagUsage</code> = [<code>warning</code> | <code>error</code>]
                    (default: not set)
                </dt>
                <dd>Lombok will flag any usage of <code>@EqualsAndHashCode</code> as a warning or error if configured.
                </dd>
            </dl>
        </div>
        <div class="overview">
            <h3>Small print</h3>

            <div class="smallprint">
                <p>
                    Arrays are 'deep' compared/hashCoded, which means that arrays that contain themselves will result in
                    <code>StackOverflowError</code>s. However, this behaviour is no different from e.g.
                    <code>ArrayList</code>.
                </p>

                <p>
                    You may safely presume that the hashCode implementation used will not change between versions of
                    lombok, however this guarantee is not set in stone; if there's a significant performance improvement
                    to be gained from using an alternate hash algorithm, that will be substituted in a future version.
                </p>

                <p>
                    For the purposes of equality, 2 <code>NaN</code> (not a number) values for floats and doubles are
                    considered equal, eventhough 'NaN == NaN' would return false. This is analogous to
                    <code>java.lang.Double</code>'s
                    equals method, and is in fact required to ensure that comparing an object to an exact copy of itself
                    returns <code>true</code> for equality.
                </p>

                <p>
                    If there is <em>any</em> method named either <code>hashCode</code> or <code>equals</code>,
                    regardless of return type, no methods will be generated, and a warning is emitted instead. These 2
                    methods need to be in sync with each other, which lombok cannot guarantee unless it generates all
                    the methods, hence you always get a warning if one <em>or</em> both of the methods already exist.
                    You can mark any method with <code>@lombok.experimental.Tolerate</code> to hide them from lombok.
                </p>

                <p>
                    Attempting to exclude fields that don't exist or would have been excluded anyway (because they are
                    static or transient) results in warnings on the named fields. You therefore don't have to worry
                    about typos.
                </p>

                <p>
                    Having both <code>exclude</code> and <code>of</code> generates a warning; the <code>exclude</code>
                    parameter will be ignored in that case.
                </p>

                <p>
                    By default, any variables that start with a $ symbol are excluded automatically. You can only
                    include them by using the 'of' parameter.
                </p>

                <p>
                    If a getter exists for a field to be included, it is called instead of using a direct field
                    reference. This behaviour can be suppressed:<br/>
                    <code>@EqualsAndHashCode(doNotUseGetters = true)</code>
                </p>
            </div>
        </div>
    </div>
</div>
</@u.page>
