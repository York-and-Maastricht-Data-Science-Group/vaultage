package org.eclipse.epsilon.emc.vaultage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.operations.declarative.CollectOperation;
import org.eclipse.epsilon.eol.function.CheckedEolFunction;
import org.eclipse.epsilon.eol.types.EolBag;
import org.eclipse.epsilon.eol.types.EolCollectionType;
import org.eclipse.epsilon.eol.types.EolSequence;

public class AsyncCollectOperation extends CollectOperation {
	
	@Override
	public Collection<?> execute(Object target, NameExpression operationNameExpression, List<Parameter> iterators,
			List<Expression> expressions, IEolContext context) throws EolRuntimeException {

		Collection<Object> source = resolveSource(target, iterators, context);
		CheckedEolFunction<Object, ?> function = resolveFunction(operationNameExpression, iterators, expressions.get(0),
				context);

		Collection<Object> result = EolCollectionType.isOrdered(source) ? new EolSequence<>() : new EolBag<>();

		if (result instanceof EolSequence) {
			((EolSequence<Object>) result).ensureCapacity(source.size());
		}

////		// parallel
//		List<Request> requests = new ArrayList<>(source.size());
//		for (Object item : source) {
//			requests.add(new Request(function, item, result));
//		}
//
//		for (Request request : requests) {
//			request.start();
//		}
//
//		for (Request request : requests) {
//			try {
//				request.join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}

		// sequential
		for (Object item : source) {
			result.add(function.applyThrows(item));
		}

		return result;
	}

	public class Request extends Thread {

		private CheckedEolFunction<Object, ?> function;
		private Object item;
		private Collection<Object> result;

		public Request(CheckedEolFunction<Object, ?> function, Object item, Collection<Object> result) {
			this.function = function;
			this.item = item;
			this.result = result;
		}

		@Override
		public void run() {
			Object temp;
			try {
				temp = function.applyThrows(this.item);
				result.add(temp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}