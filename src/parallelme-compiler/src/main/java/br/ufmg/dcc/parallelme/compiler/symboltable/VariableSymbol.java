/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / _ / /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 *  DCC-UFMG
 */

package br.ufmg.dcc.parallelme.compiler.symboltable;

/**
 * A symbol for variables definition on the symbol table.
 * 
 * @author Wilson de Carvalho
 * @see Symbol
 */
public class VariableSymbol extends Symbol {
	public final String typeName;
	public final String typeParameterName;
	public final TokenAddress statementAddress;

	public VariableSymbol(String name, String typeName,
			String typeParameterName, Symbol enclosingScope,
			TokenAddress tokenAddress, TokenAddress statementAddress) {
		super(name, enclosingScope, tokenAddress);
		this.typeName = typeName;
		this.typeParameterName = typeParameterName;
		this.statementAddress = statementAddress;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String readableTableHeader() {
		return super.readableTableHeader() + ", " + typeName + ", "
				+ typeParameterName;
	}

	@Override
	public boolean equals(Object other) {
		if (other.getClass() == this.getClass()) {
			VariableSymbol foo = (VariableSymbol) other;
			return super.equals(other) && foo.typeName == this.typeName
					&& foo.typeParameterName == this.typeParameterName
					&& foo.statementAddress == this.statementAddress;
		} else {
			return false;
		}
	}
}
