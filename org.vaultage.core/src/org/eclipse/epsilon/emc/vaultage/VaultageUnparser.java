package org.eclipse.epsilon.emc.vaultage;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.parse.EolUnparser;

public class VaultageUnparser extends EolUnparser {

	/***
	 * Move the buffer string to a temporary String then move it back to the buffer
	 * after creating the statement string of the expression.
	 * 
	 * @param moduleElement
	 * @return
	 */
	public String unparse(ModuleElement moduleElement) {

		String originalBuffer = new String(buffer.toString());

		buffer.setLength(0);
		if (moduleElement instanceof FirstOrderOperationCallExpression) {
			this.visit((FirstOrderOperationCallExpression) moduleElement);
		} else if (moduleElement instanceof PropertyCallExpression) {
			this.visit((PropertyCallExpression) moduleElement);
		} else if (moduleElement instanceof NameExpression) {
			this.visit((NameExpression) moduleElement);
		}
		String statement = new String(buffer.toString());

		buffer.setLength(0);
		buffer.append(originalBuffer);

		return statement;
	}

}
