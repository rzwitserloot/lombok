<#import "../../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <div class="header-group">
            <h1>onX</h1>

            <h3>Sup dawg, we heard you like annotations, so we put annotations in your annotations so
                you can annotate while you're annotating.
            </h3>

            <p>
                onX was introduced as experimental feature in lombok v0.11.8.
            </p>
        </div>
    </div>
    <div class="row">
        <h3>Experimental</h3>

        <p>
            Experimental because:
        <ul>
            <li>Ugly syntax. The syntax of this feature is not optimal, but it is the least convoluted syntax that could
                possibly work (for now!)
            </li>
            <li>Possibly java 9 will offer (much) better ways of supporting this feature.</li>
            <li>Uncertainty: Future versions of javac may break this feature, and we may not be able to restore it.</li>
        </ul>
        Current status: <em>uncertain</em> - Currently we feel this feature cannot move out of experimental status.
    </div>
    <div class="row">
        <h3>Overview</h3>

        <p>
            <strong>This feature is considered 'workaround status' - it exists in order to allow users of lombok that
                cannot work without this feature to have access to it anyway. If we find a better way to implement this
                feature, or some future java version introduces an alternative strategy, this feature can disappear
                without a reasonable deprecation period. Also, this feature may not work in future versions of javac.
                Use at your own discretion.</strong>
        </p>

        <p>
            Most annotations that make lombok generate methods or constructors can be configured to also make lombok put
            custom annotations on elements in the generated code.
        </p>

        <p>
            <code>@Getter</code>, <code>@Setter</code>, and <code>@Wither</code> support the <code>onMethod</code>
            option, which will put the listed annotations on the generated method.
        </p>

        <p>
            <code>@AllArgsConstructor</code>, <code>@NoArgsConstructor</code>, and <code>@RequiredArgsConstructor</code>
            support the <code>onConstructor</code> option which will put the listed annotations on the generated
            constructor.
        </p>

        <p>
            <code>@Setter</code> and <code>@Wither</code> support <code>onParam</code> in addition to
            <code>onMethod</code>; annotations listed will be put on the only parameter that the generated method has.
            <code>@EqualsAndHashCode</code> also supports <code>onParam</code>; the listed annotation(s) will be placed
            on the single parameter of the generated <code>equals</code> method, as well as any generated
            <code>canEqual</code> method.
        </p>

        <p>
            The syntax is a little strange; to use any of the 3 <code>onX</code> features, you must wrap the annotations
            to be applied to the constructor / method / parameter in <code>@__(@AnnotationGoesHere)</code>. To apply
            multiple annotations, use <code>@__({@Annotation1, @Annotation2})</code>. The annotations can themselves
            obviously have parameters as well.
        </p>
    </div>
    <@u.comparison />
    <div class="row">
        <h3>Small print</h3>

        <div class="smallprint">
            <p>
                The reason of the weird syntax is to make this feature work in javac 7 compilers; the <code>@__</code>
                type is an annotation reference to the annotation type <code>__</code> (double underscore) which doesn't
                actually exist; this makes javac 7 delay aborting the compilation process due to an error because it is
                possible an annotation processor will later create the <code>__</code> type. Instead, lombok applies the
                annotations and removes the references so that the error will never actually occur. The point is: The
                <code>__</code> type <em>must not exist</em>, otherwise the feature does not work. In the rare case that
                the <code>__</code> type does exist (and is imported or in the package), you can simply add more
                underscores. Technically any non-existent type would work, but to maintain consistency and readability
                and catch erroneous use, lombok considers it an error if the 'wrapper' annotation is anything but a
                series of underscores.
            </p>

            <p>
                To reiterate: This feature can disappear at any time; if you use this feature, be prepared to adjust
                your code when we find a nicer way of implementing this feature, or, if a future version of javac forces
                us to remove this feature entirely with no alternative.
            </p>

            <p>
                The <code>onX</code> parameter is not legal on any type-wide variant. For example, a
                <code>@Getter</code> annotation on a class does not support <code>onMethod</code>.
            </p>
        </div>
    </div>
</div>
</@u.page>
