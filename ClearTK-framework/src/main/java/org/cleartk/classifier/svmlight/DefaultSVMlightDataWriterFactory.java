/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */

package org.cleartk.classifier.svmlight;

import java.io.IOException;

import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.DataWriterFactory_ImplBase;
import org.cleartk.classifier.encoder.features.BooleanEncoder;
import org.cleartk.classifier.encoder.features.FeatureVectorFeaturesEncoder;
import org.cleartk.classifier.encoder.features.NumberEncoder;
import org.cleartk.classifier.encoder.features.StringEncoder;
import org.cleartk.classifier.encoder.features.normalizer.EuclidianNormalizer;
import org.cleartk.classifier.encoder.features.normalizer.NameNumberNormalizer;
import org.cleartk.classifier.encoder.outcome.BooleanToBooleanOutcomeEncoder;
import org.cleartk.classifier.util.featurevector.FeatureVector;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * @author Philipp Wetzler
 * 
 */

public class DefaultSVMlightDataWriterFactory extends DataWriterFactory_ImplBase<FeatureVector, Boolean, Boolean> {

	public DataWriter<Boolean> createDataWriter() throws IOException {
		SVMlightDataWriter dataWriter = new SVMlightDataWriter(outputDirectory);

		if(!this.setEncodersFromFileSystem(dataWriter)) {
			NameNumberNormalizer normalizer = new EuclidianNormalizer();
			FeatureVectorFeaturesEncoder featuresEncoder = new FeatureVectorFeaturesEncoder(normalizer);
			featuresEncoder.addEncoder(new NumberEncoder());
			featuresEncoder.addEncoder(new BooleanEncoder());
			featuresEncoder.addEncoder(new StringEncoder());
			dataWriter.setFeaturesEncoder(featuresEncoder);
			dataWriter.setOutcomeEncoder(new BooleanToBooleanOutcomeEncoder());
		}

		return dataWriter;
	}

}
