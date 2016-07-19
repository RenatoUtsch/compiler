/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / __/ /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 */

package org.parallelme.compiler.userlibrary.classes;

import java.util.HashSet;

import org.parallelme.compiler.userlibrary.UserLibraryClassImpl;

/**
 * Defines the user library class RGBA.
 * 
 * @author Wilson de Carvalho
 */
public class RGBA extends UserLibraryClassImpl {
	private static RGBA instance = new RGBA();

	private RGBA() {
		this.initValidMethodsSet();
	}

	public static RGBA getInstance() {
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTyped() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initValidMethodsSet() {
		this.validMethods = new HashSet<>();
	}

	/**
	 * {@inheritDoc}
	 */
	public static String getName() {
		return "RGBA";
	}
}