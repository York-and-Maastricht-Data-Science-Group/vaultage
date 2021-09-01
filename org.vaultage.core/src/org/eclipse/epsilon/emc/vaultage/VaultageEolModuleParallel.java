package org.eclipse.epsilon.emc.vaultage;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.common.parse.AST;
import org.eclipse.epsilon.eol.concurrent.EolModuleParallel;
import org.eclipse.epsilon.eol.dom.FirstOrderOperationCallExpression;
import org.eclipse.epsilon.eol.dom.OperationCallExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.parse.EolParser;

public class VaultageEolModuleParallel extends EolModuleParallel {

	public VaultageEolModuleParallel(VaultageEolContextParallel context) {
		super(context);
	}

	public ModuleElement adapt(org.eclipse.epsilon.common.parse.AST cst, ModuleElement parentAst) {

		ModuleElement element = super.adapt(cst, parentAst);

		if (element instanceof PropertyCallExpression) {
			element = new VaultagePropertyCallExpression();
		} else if (element instanceof FirstOrderOperationCallExpression) {
			element = new VaultageFirstOrderOperationCallExpression();
		} else if (element instanceof OperationCallExpression) {
			AST cstParent = cst.getParent();
			int parentType = cst.getType();
			if (cst.hasChildren() && cst.getFirstChild().getType() == EolParser.PARAMETERS
					&& ((parentType != EolParser.ARROW && parentType != EolParser.POINT
							&& parentType != EolParser.NAVIGATION)
							|| (parentType == EolParser.ARROW || parentType == EolParser.POINT
									|| parentType == EolParser.NAVIGATION) && cstParent.getFirstChild() == cst)) {
				element = new VaultageOperationCallExpression(true);
			} else {
				element = new VaultageOperationCallExpression();
			}
		}
		return element;
	}
}