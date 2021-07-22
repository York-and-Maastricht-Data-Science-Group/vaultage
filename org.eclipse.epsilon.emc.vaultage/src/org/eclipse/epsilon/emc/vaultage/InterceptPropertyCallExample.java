package org.eclipse.epsilon.emc.vaultage;

import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;

public class InterceptPropertyCallExample {
	
	public static void main(String[] args) throws Exception {
		new InterceptPropertyCallExample().run();
	}
	
	public void run() throws Exception {
		EolModule module = new EolModule() {
			public ModuleElement adapt(org.eclipse.epsilon.common.parse.AST cst, ModuleElement parentAst) {
				ModuleElement element = super.adapt(cst, parentAst);
				if (element instanceof PropertyCallExpression) {
					element = new InterceptingPropertyCallExpression();
				}
				return element;
			};
		};
		module.parse("var res = 'Hello world'.foo.println();");
		
		module.execute();
	}
	
	public class InterceptingPropertyCallExpression extends PropertyCallExpression {
		@Override
		public Object execute(Object source, NameExpression propertyNameExpression, IEolContext context)
				throws EolRuntimeException {
			
			if (source instanceof String) {
				return source + propertyNameExpression.getName();
			}
			
			return super.execute(source, propertyNameExpression, context);
		}
	}
	
}
