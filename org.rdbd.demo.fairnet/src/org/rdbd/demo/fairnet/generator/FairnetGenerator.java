package org.rdbd.demo.fairnet.generator;

import java.io.File;

import org.eclipse.epsilon.egl.EglFileGeneratingTemplateFactory;
import org.eclipse.epsilon.egl.EgxModule;
import org.eclipse.epsilon.emc.emf.EmfModel;

/***
 * A class to generate the Java code of Fairnet
 */
public class FairnetGenerator {

	public static void main(String[] args) throws Exception {
		FairnetGenerator gen = new FairnetGenerator();
		gen.generate("egx/fairnet.egx", "model/fairnet.ecore");
		System.out.println("Finished");
	}

	/***
	 * Generate the Java code of Fairnet from the supplied EGX and model files
	 * @param egxFilePath
	 * @param modelFilePath
	 * @throws Exception 
	 */
	public void generate(String egxFilePath, String modelFilePath) throws Exception {
		
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
//		module.getGenerationRules().get(0).
		module.execute();
	}
}
