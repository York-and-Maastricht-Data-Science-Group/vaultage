package org.eclipse.epsilon.emc.vaultage;

import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;

public class VaultagePropertyCallExpression extends PropertyCallExpression {
		@Override
		public Object execute(Object source, NameExpression propertyNameExpression, IEolContext context)
				throws EolRuntimeException {
			
//			if (source instanceof String) {
//				return source + propertyNameExpression.getName();
//			}
			
			return super.execute(source, propertyNameExpression, context);
		}
	}