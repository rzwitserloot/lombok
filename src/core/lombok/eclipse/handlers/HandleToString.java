/*
 * Copyright (C) 2009-2014 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.eclipse.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.ToString;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.FieldAccess;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@code ToString} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleToString extends EclipseAnnotationHandler<ToString> {
	private void checkForBogusFieldNames(EclipseNode type, AnnotationValues<ToString> annotation, List<String> excludes, List<String> includes) {
		if (annotation.isExplicit("exclude")) {
			for (int i : createListOfNonExistentFields(excludes, type, true, false)) {
				annotation.setWarning("exclude", "This field does not exist, or would have been excluded anyway.", i);
			}
		}
		if (annotation.isExplicit("of")) {
			for (int i : createListOfNonExistentFields(includes, type, false, false)) {
				annotation.setWarning("of", "This field does not exist.", i);
			}
		}
	}

	@Override
	public void handle(AnnotationValues<ToString> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.TO_STRING_FLAG_USAGE, "@ToString");
		
		ToString ann = annotation.getInstance();
		List<String> excludes = Arrays.asList(ann.exclude());
		List<String> includes = Arrays.asList(ann.of());
		EclipseNode typeNode = annotationNode.up();

		if (excludes != null && excludes.size() != 0 && includes != null && includes.size() != 0) {
			excludes = null;
			annotation.setWarning("exclude", "exclude and of are mutually exclusive; the 'exclude' parameter will be ignored.");
		}
		checkForBogusFieldNames(typeNode, annotation, excludes, includes);

		Boolean fqn = ann.fqn();
		Boolean callSuper = ann.callSuper();
		String prefix = ann.prefix();
		String separator = ann.separator();
		String infix = ann.infix();
		String suffix = ann.suffix();

		if (!annotation.isExplicit("fqn")) fqn = null;
		if (!annotation.isExplicit("callSuper")) callSuper = null;
		if (!annotation.isExplicit("exclude")) excludes = null;
		if (!annotation.isExplicit("of")) includes = null;
		if (!annotation.isExplicit("prefix")) prefix = null;
		if (!annotation.isExplicit("separator")) separator = null;
		if (!annotation.isExplicit("infix")) infix = null;
		if (!annotation.isExplicit("suffix")) suffix = null;

		Boolean doNotUseGettersConfiguration = annotationNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_DO_NOT_USE_GETTERS);
		boolean doNotUseGetters = annotation.isExplicit("doNotUseGetters") || doNotUseGettersConfiguration == null ? ann.doNotUseGetters() : doNotUseGettersConfiguration;
		FieldAccess fieldAccess = doNotUseGetters ? FieldAccess.PREFER_FIELD : FieldAccess.GETTER;
		
		Boolean fieldNamesConfiguration = annotationNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_INCLUDE_FIELD_NAMES);
		boolean includeFieldNames = annotation.isExplicit("includeFieldNames") || fieldNamesConfiguration == null ? ann.includeFieldNames() : fieldNamesConfiguration;

		generateToString(typeNode, annotationNode, fqn, prefix, callSuper, includeFieldNames, separator, fieldAccess, excludes, includes, infix, suffix, true);
	}

	public void generateToStringForType(EclipseNode typeNode, EclipseNode errorNode) {
		if (hasAnnotation(ToString.class, typeNode)) {
			//The annotation will make it happen, so we can skip it.
			return;
		}

		boolean includeFieldNames = true;
		try {
			Boolean configuration = typeNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_INCLUDE_FIELD_NAMES);
			includeFieldNames = configuration != null ? configuration : ((Boolean)ToString.class.getMethod("includeFieldNames").getDefaultValue()).booleanValue();
		} catch (Exception ignore) {}

		Boolean doNotUseGettersConfiguration = typeNode.getAst().readConfiguration(ConfigurationKeys.TO_STRING_DO_NOT_USE_GETTERS);
		FieldAccess access = doNotUseGettersConfiguration == null || !doNotUseGettersConfiguration ? FieldAccess.GETTER : FieldAccess.PREFER_FIELD;

		generateToString(typeNode, errorNode, null, null, null, includeFieldNames, null, access, null, null, null, null, false);
	}

	private void generateToString(EclipseNode typeNode, EclipseNode errorNode, Boolean fqn, String prefix, Boolean callSuper,
								  boolean includeFieldNames, String separator, FieldAccess fieldAccess, List<String> excludes, List<String> includes,
								  String infix, String suffix, boolean whineIfExists) {
		TypeDeclaration typeDecl = null;
		
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;
		
		if (typeDecl == null || notAClass) {
			errorNode.addError("@ToString is only supported on a class or enum.");
		}

		if (fqn == null) {
			try {
				fqn = ((Boolean)ToString.class.getMethod("fqn").getDefaultValue()).booleanValue();
			} catch (Exception ignore) {}
		}

		if (prefix == null) {
			try {
				prefix = ((String)ToString.class.getMethod("prefix").getDefaultValue());
			} catch (Exception ignore) {}
		}

		if (callSuper == null) {
			try {
				callSuper = ((Boolean)ToString.class.getMethod("callSuper").getDefaultValue()).booleanValue();
			} catch (Exception ignore) {}
		}

		if (separator == null) {
			try {
				separator = ((String)ToString.class.getMethod("separator").getDefaultValue());
			} catch (Exception ignore) {}
		}

		List<EclipseNode> nodesForToString = new ArrayList<EclipseNode>();
		if (includes != null) {
			for (EclipseNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
				if (includes.contains(new String(fieldDecl.name))) nodesForToString.add(child);
			}
		} else {
			for (EclipseNode child : typeNode.down()) {
				if (child.getKind() != Kind.FIELD) continue;
				FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
				if (!filterField(fieldDecl)) continue;
				
				//Skip excluded fields.
				if (excludes != null && excludes.contains(new String(fieldDecl.name))) continue;
				
				nodesForToString.add(child);
			}
		}

		if (infix == null) {
			try {
				infix = ((String)ToString.class.getMethod("infix").getDefaultValue());
			} catch (Exception ignore) {}
		}

		if (suffix == null) {
			try {
				suffix = ((String)ToString.class.getMethod("suffix").getDefaultValue());
			} catch (Exception ignore) {}
		}

		switch (methodExists("toString", typeNode, 0)) {
		case NOT_EXISTS:
			MethodDeclaration toString = createToString(typeNode, errorNode.get(), fqn, prefix, callSuper, includeFieldNames,
					separator, fieldAccess, nodesForToString, infix, suffix);
			injectMethod(typeNode, toString);
			break;
		case EXISTS_BY_LOMBOK:
			break;
		default:
		case EXISTS_BY_USER:
			if (whineIfExists) {
				errorNode.addWarning("Not generating toString(): A method with that name already exists");
			}
		}
	}

	static MethodDeclaration createToString(EclipseNode type, ASTNode source, Boolean fqn, String prefix, boolean callSuper,
											boolean includeFieldNames, String separator, FieldAccess fieldAccess,
											Collection<EclipseNode> fields, String infix, String suffix) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		final int PLUS = OperatorIds.PLUS;

		char[] resultArray;
		String typeName = getTypeName(type, fqn);
		if (callSuper) {
			resultArray = (typeName + prefix + "super" + separator).toCharArray();
		} else if (fields.isEmpty()) {
			resultArray = (typeName + prefix + suffix).toCharArray();
		} else if (includeFieldNames) {
			resultArray = (typeName + prefix + new String(((FieldDeclaration)fields.iterator().next().get()).name) + separator).toCharArray();
		} else {
			resultArray = (typeName + prefix).toCharArray();
		}
		
		boolean first = true;
		Expression current = new StringLiteral(resultArray, pS, pE, 0);
		setGeneratedBy(current, source);
		
		if (callSuper) {
			MessageSend callToSuper = new MessageSend();
			callToSuper.sourceStart = pS; callToSuper.sourceEnd = pE;
			setGeneratedBy(callToSuper, source);
			callToSuper.receiver = new SuperReference(pS, pE);
			setGeneratedBy(callToSuper, source);
			callToSuper.selector = "toString".toCharArray();
			current = new BinaryExpression(current, callToSuper, PLUS);
			setGeneratedBy(current, source);
			first = false;
		}
		
		for (EclipseNode field : fields) {
			TypeReference fieldType = getFieldType(field, fieldAccess);
			Expression fieldAccessor = createFieldAccessor(field, fieldAccess, source);
			
			// The distinction between primitive and object will be useful if we ever add a 'hideNulls' option.
			boolean fieldBaseTypeIsPrimitive = BUILT_IN_TYPES.contains(new String(fieldType.getLastToken()));
			boolean fieldIsPrimitive = fieldType.dimensions() == 0 && fieldBaseTypeIsPrimitive;
			boolean fieldIsPrimitiveArray = fieldType.dimensions() == 1 && fieldBaseTypeIsPrimitive;
			boolean fieldIsObjectArray = fieldType.dimensions() > 0 && !fieldIsPrimitiveArray;
			@SuppressWarnings("unused")
			boolean fieldIsObject = !fieldIsPrimitive && !fieldIsPrimitiveArray && !fieldIsObjectArray;
			
			Expression ex;
			if (fieldIsPrimitiveArray || fieldIsObjectArray) {
				MessageSend arrayToString = new MessageSend();
				arrayToString.sourceStart = pS; arrayToString.sourceEnd = pE;
				arrayToString.receiver = generateQualifiedNameRef(source, TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				arrayToString.arguments = new Expression[] { fieldAccessor };
				setGeneratedBy(arrayToString.arguments[0], source);
				arrayToString.selector = (fieldIsObjectArray ? "deepToString" : "toString").toCharArray();
				ex = arrayToString;
			} else {
				ex = fieldAccessor;
			}
			setGeneratedBy(ex, source);
			
			if (first) {
				current = new BinaryExpression(current, ex, PLUS);
				current.sourceStart = pS; current.sourceEnd = pE;
				setGeneratedBy(current, source);
				first = false;
				continue;
			}
			
			StringLiteral fieldNameLiteral;
			if (includeFieldNames) {
				char[] namePlusEqualsSign = (infix + field.getName() + separator).toCharArray();
				fieldNameLiteral = new StringLiteral(namePlusEqualsSign, pS, pE, 0);
			} else {
				fieldNameLiteral = new StringLiteral(infix.toCharArray(), pS, pE, 0);
			}
			setGeneratedBy(fieldNameLiteral, source);
			current = new BinaryExpression(current, fieldNameLiteral, PLUS);
			setGeneratedBy(current, source);
			current = new BinaryExpression(current, ex, PLUS);
			setGeneratedBy(current, source);
		}
		if (!first) {
			StringLiteral suffixLiteral = new StringLiteral(suffix.toCharArray(), pS, pE, 0);
			setGeneratedBy(suffixLiteral, source);
			current = new BinaryExpression(current, suffixLiteral, PLUS);
			setGeneratedBy(current, source);
		}
		
		ReturnStatement returnStatement = new ReturnStatement(current, pS, pE);
		setGeneratedBy(returnStatement, source);
		
		MethodDeclaration method = new MethodDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
		setGeneratedBy(method, source);
		method.modifiers = toEclipseModifier(AccessLevel.PUBLIC);
		method.returnType = new QualifiedTypeReference(TypeConstants.JAVA_LANG_STRING, new long[] {p, p, p});
		setGeneratedBy(method.returnType, source);
		method.annotations = new Annotation[] {makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, source)};
		method.arguments = null;
		method.selector = "toString".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		method.statements = new Statement[] { returnStatement };
		return method;
	}
	
	private static String getTypeName(EclipseNode type, Boolean fqn) {
		String typeName = getSingleTypeName(type);
		EclipseNode upType = type.up();
		while (upType.getKind() == Kind.TYPE) {
			typeName = getSingleTypeName(upType) + "." + typeName;
			upType = upType.up();
		}
		if (fqn) {
			String packageDeclaration = upType.getPackageDeclaration();
			typeName = (packageDeclaration == null ? "" : packageDeclaration + ".") + typeName;
		}
		return typeName;
	}
	
	private static String getSingleTypeName(EclipseNode type) {
		TypeDeclaration typeDeclaration = (TypeDeclaration)type.get();
		char[] rawTypeName = typeDeclaration.name;
		return rawTypeName == null ? "" : new String(rawTypeName);
	}
	
	private static final Set<String> BUILT_IN_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"byte", "short", "int", "long", "char", "boolean", "double", "float")));
	
	private static NameReference generateQualifiedNameRef(ASTNode source, char[]... varNames) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		NameReference ref;
		if (varNames.length > 1) ref = new QualifiedNameReference(varNames, new long[varNames.length], pS, pE);
		else ref = new SingleNameReference(varNames[0], p);
		setGeneratedBy(ref, source);
		return ref;
	}
}
