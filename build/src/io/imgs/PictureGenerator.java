package io.imgs;

import org.openscience.cdk.renderer.generators.BasicAtomGenerator;

public class PictureGenerator extends AbstractPictureGenerator {

	public PictureGenerator() {
		super(new BasicAtomGenerator());
	}

}
