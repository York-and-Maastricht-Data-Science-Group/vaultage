package org.vaultage.generator;

import java.io.File;

import org.eclipse.epsilon.egl.EglFileGeneratingTemplateFactory;
import org.eclipse.epsilon.egl.EgxModule;
import org.eclipse.epsilon.emc.emf.EmfModel;

public class VaultageGenerator {
	
	/***
	 * Generate the Java code implementation from the supplied model file. The default EGX file path is generator/vaultage.egx" 
	 *  
	 * @param modelFilePath
	 * @throws Exception
	 */
	public static void generate(String modelFilePath) throws Exception {
		generate("generator/vaultage.egx", modelFilePath);
	}
	
	/***
	 * Generate the Java code implementation from the supplied EGX and model files
	 *  
	 * @param egxFilePath
	 * @param modelFilePath
	 * @throws Exception
	 */
	public static void generate(String egxFilePath, String modelFilePath) throws Exception {

		EgxModule module = new EgxModule(new EglFileGeneratingTemplateFactory());
		module.parse(new File(egxFilePath).getAbsoluteFile());
 
		if (!module.getParseProblems().isEmpty()) {
			System.out.println("Syntax errors found. Exiting.");
			return;
		}

		EmfModel model = new EmfModel();
		model.setModelFile(modelFilePath);
		model.load();

		module.getContext().getModelRepository().addModel(model);
		module.execute();
	}
}
