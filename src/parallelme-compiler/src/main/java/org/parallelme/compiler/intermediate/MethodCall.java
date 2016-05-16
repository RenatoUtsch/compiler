/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / __/ /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 */

package org.parallelme.compiler.intermediate;

import org.parallelme.compiler.symboltable.TokenAddress;

/**
 * Intermediate representation for regular method call (non-iterator, non-bind)
 * on a given object.
 * 
 * @author Wilson de Carvalho
 */
public class MethodCall {
	public final String methodName;
	public final Variable variable;
	public final TokenAddress expressionAddress;

	public MethodCall(String methodName, Variable variable,
			TokenAddress statementAddress) {
		this.methodName = methodName;
		this.variable = variable;
		this.expressionAddress = statementAddress;
	}

	@Override
	public int hashCode() {
		return this.methodName.hashCode() * 3 + this.variable.hashCode() * 7
				+ this.expressionAddress.hashCode() * 11;
	}
}