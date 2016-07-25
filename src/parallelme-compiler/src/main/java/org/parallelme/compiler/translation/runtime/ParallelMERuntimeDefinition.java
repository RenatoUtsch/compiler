/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / __/ /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 */

package org.parallelme.compiler.translation.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.parallelme.compiler.RuntimeCommonDefinitions;
import org.parallelme.compiler.RuntimeDefinitionImpl;
import org.parallelme.compiler.exception.CompilationException;
import org.parallelme.compiler.intermediate.*;
import org.parallelme.compiler.intermediate.Operation.ExecutionType;
import org.parallelme.compiler.translation.CTranslator;
import org.parallelme.compiler.userlibrary.classes.*;
import org.parallelme.compiler.util.ResourceWriter;

/**
 * General definitions for ParallelME runtime.
 * 
 * @author Wilson de Carvalho
 */
public class ParallelMERuntimeDefinition extends RuntimeDefinitionImpl {
	public ParallelMERuntimeDefinition(CTranslator cCodeTranslator,
			String outputDestinationFolder) {
		super(cCodeTranslator, outputDestinationFolder);
		this.initTranslators();
	}

	private void initTranslators() {
		if (super.translators == null) {
			super.translators = new HashMap<>();
			super.translators.put(Array.getInstance().getClassName(),
					new PMArrayTranslator(cCodeTranslator));
			super.translators.put(BitmapImage.getInstance().getClassName(),
					new PMBitmapImageTranslator(cCodeTranslator));
			super.translators.put(HDRImage.getInstance().getClassName(),
					new PMHDRImageTranslator(cCodeTranslator));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TargetRuntime getTargetRuntime() {
		return TargetRuntime.ParallelME;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getIsValidBody() {
		ArrayList<String> ret = new ArrayList<>();
		ret.add("return isValid && ParallelMERuntime.getInstance().runtimePointer != 0;");
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getInitializationString(String className,
			OperationsAndBinds operationsAndBinds, List<MethodCall> methodCalls)
			throws CompilationException {
		List<String> ret = new ArrayList<>();
		ret.add("private static boolean isValid;");
		ret.add(" ");
		// Declare native functions to call NDK
		ret.addAll(this.declareNativeOperations(operationsAndBinds.operations));
		ret.add(" ");
		ret.addAll(this.cleanUpPointers(operationsAndBinds.inputBinds));
		ret.add(" ");
		ret.addAll(this.initializeParallelME());
		return ret;
	}

	private List<String> declareNativeOperations(List<Operation> operations)
			throws CompilationException {
		List<String> ret = new ArrayList<>();
		Variable runtimePtr = new Variable("runtimePtr", "long", null, "", -1);
		Variable variablePointer = new Variable("varPtr", "long", null, "", -1);
		for (Operation operation : operations) {
			List<Parameter> parameters = new ArrayList<>();
			parameters.add(runtimePtr);
			parameters.add(variablePointer);
			// Sequential operations must create an array for each variable.
			// This array will be used to store the output value.
			List<Variable> externalVariables = operation.getExternalVariables();
			boolean isSequential = operation.getExecutionType() == ExecutionType.Sequential;
			if (isSequential) {
				for (Variable foo : externalVariables) {
					parameters.add(foo);
					if (!foo.isFinal())
						parameters.add(new Variable(RuntimeCommonDefinitions
								.getInstance().getPrefix() + foo.name,
								foo.typeName + "[]", null, "", -1));
				}
			} else {
				parameters.addAll(externalVariables);
			}
			if (operation.destinationVariable != null) {
				// Last parameter is the destination variable
				parameters.add(this
						.createDestinationVariableParameter(operation));
			}
			String name = RuntimeCommonDefinitions.getInstance()
					.getOperationName(operation);
			ret.add(RuntimeCommonDefinitions.getInstance()
					.createJavaMethodSignature("private native", "void", name,
							parameters, false)
					+ ";");
		}
		return ret;
	}

	/**
	 * Creates a temporary variable that is used to describe the destination
	 * variable.
	 */
	private Variable createDestinationVariableParameter(Operation operation) {
		Variable variable;
		if (operation.variable.typeName.equals(BitmapImage.getInstance()
				.getClassName())
				|| operation.variable.typeName.equals(HDRImage.getInstance()
						.getClassName())) {
			// translateType call must inform the operation variable, not the
			// destination variable, since it is based on the image class type
			variable = new Variable(RuntimeCommonDefinitions.getInstance()
					.getPrefix() + operation.destinationVariable.name,
					RuntimeCommonDefinitions.getInstance().translateToCType(
							operation.variable.typeName)
							+ "[]", null, "", -1);
		} else {
			variable = new Variable(RuntimeCommonDefinitions.getInstance()
					.getPrefix() + operation.destinationVariable.name,
					RuntimeCommonDefinitions.getInstance().translateToCType(
							operation.destinationVariable.typeName)
							+ "[]", null, "", -1);
		}
		return variable;
	}

	private List<String> cleanUpPointers(List<InputBind> inputBinds) {
		ArrayList<String> ret = new ArrayList<>();
		Set<Variable> variables = new HashSet<>();
		for (InputBind inputBind : inputBinds)
			variables.add(inputBind.variable);
		ret.add("@Override");
		ret.add("protected void finalize() throws Throwable {");
		ret.add("\tsuper.finalize();");
		ret.add("}");
		ret.add("");
		return ret;
	}

	private List<String> initializeParallelME() {
		ArrayList<String> ret = new ArrayList<>();
		ret.add("static {");
		ret.add("\ttry {");
		ret.add("\t\tSystem.loadLibrary(\"ParallelMEGenerated\");");
		ret.add("\t\tisValid = true;");
		ret.add("\t} catch (UnsatisfiedLinkError e) {");
		ret.add("\t\tisValid = false;");
		ret.add("\t}");
		ret.add("}");
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getImports() {
		ArrayList<String> ret = new ArrayList<>();
		ret.add("org.parallelme.ParallelMERuntime");
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void translateOperationsAndBinds(String packageName,
			String className, OperationsAndBinds operationsAndBinds) {
		ParallelMERuntimeCTranslation cTranslation = new ParallelMERuntimeCTranslation();
		cTranslation.createKernelFile(className, operationsAndBinds,
				this.translators, this.outputDestinationFolder);
		String cClassName = RuntimeCommonDefinitions.getInstance()
				.getJavaWrapperClassName(className, TargetRuntime.ParallelME);
		cTranslation.createCPPFile(packageName, cClassName,
				operationsAndBinds.operations, this.outputDestinationFolder);
		cTranslation.createHFile(packageName, cClassName,
				operationsAndBinds.operations, this.outputDestinationFolder);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exportInternalLibrary(String packageName,
			String destinationFolder) throws IOException {
		ResourceWriter.exportResource("ParallelME", destinationFolder);
	}
}