package org.eclipse.epsilon.emc.vaultage;

import java.util.Map;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.dom.AssignmentStatement;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.LiteralExpression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.dom.VariableDeclaration;
import org.eclipse.epsilon.eol.parse.EolUnparser;
import org.eclipse.epsilon.eol.types.EolMap;

public class VaultageEolUnparser extends EolUnparser {

	protected final EolMap<String, Object> variables = new EolMap<>();

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
		} else if (moduleElement instanceof OperationCallExpression) {
			this.visit((OperationCallExpression) moduleElement);
		} 
		else if (moduleElement instanceof PropertyCallExpression) {
			this.visit((PropertyCallExpression) moduleElement);
		} 
		else if (moduleElement instanceof NameExpression) {
			this.visit((NameExpression) moduleElement);
		}
		String statement = new String(buffer.toString());

		buffer.setLength(0);
		buffer.append(originalBuffer);

		return statement;
	}
	
//	@Override
//	public void visit(VariableDeclaration variableDeclaration) {
//		String name = variableDeclaration.getName();
////		if (name.equals("condition")) {
////			System.console();
////		}
//		Expression expression = ((AssignmentStatement) variableDeclaration.getParent()).getValueExpression();
//		if (expression instanceof LiteralExpression<?>) {
//			Object value = ((LiteralExpression<?>) expression).getValue();
//			variables.put(name, value);
//		} 
//		super.visit(variableDeclaration);
//	}
	
	public EolMap<String, Object> getVariables() {
		return variables;
	}

}
