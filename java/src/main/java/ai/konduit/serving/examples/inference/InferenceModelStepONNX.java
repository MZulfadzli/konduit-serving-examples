/*
 *       Copyright (c) 2019 Konduit AI.
 *
 *       This program and the accompanying materials are made available under the
 *       terms of the Apache License, Version 2.0 which is available at
 *       https://www.apache.org/licenses/LICENSE-2.0.
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *       WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *       License for the specific language governing permissions and limitations
 *       under the License.
 *
 *       SPDX-License-Identifier: Apache-2.0
 *
 */
package ai.konduit.serving.examples.inference;

import ai.konduit.serving.InferenceConfiguration;
import ai.konduit.serving.config.ServingConfig;
import ai.konduit.serving.configprovider.KonduitServingMain;
import ai.konduit.serving.model.PythonConfig;
import ai.konduit.serving.pipeline.step.ImageLoadingStep;
import ai.konduit.serving.pipeline.step.PythonStep;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.io.FileUtils;
import org.datavec.python.PythonVariables;
import org.nd4j.base.Preconditions;
import org.nd4j.linalg.io.ClassPathResource;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;

@NotThreadSafe

/*
 * Example for Inference for ONNX ML model using python step .
 * This illustrates only the server configuration and start server.
 */
class InferenceModelStepONNX {
    public static void main(String[] args) throws Exception {

        String pythonCodePath = new ClassPathResource("scripts/onnxFacedetect.py").getFile().getAbsolutePath();

        /* To run this example please install (PIL 6.21, numpy, onnxruntime 1.1.0, torchvision 0.4.2) and
         * set the "pythonPath" variable in this example to refer to the required Python libraries.
         * To find the python path, execute the following script on CLI:
         * python -c "import sys, os; print(os.pathsep.join([path for path in sys.path if path]))"
         * and replace the variable down below...
         */
        String pythonPath = null;

        Preconditions.checkNotNull(pythonPath, "\"pythonPath\" variable shouldn't be null. " +
                "\n\nTo run this example please install (PIL 6.21, numpy, onnxruntime 1.1.0, torchvision 0.4.2) and\n" +
                "set the \"pythonPath\" variable in this example to refer to the required Python libraries.\n" +
                "To find the python path, execute the following script on CLI:\n" +
                "\npython -c \"import sys, os; print(os.pathsep.join([path for path in sys.path if path]))\"\n\n");

        //python configuration for input and output.
        PythonConfig python_config = PythonConfig.builder()
                .pythonCodePath(pythonCodePath)
                .pythonInput("inputimage", PythonVariables.Type.NDARRAY.name())
                .pythonOutput("boxes", PythonVariables.Type.NDARRAY.name())
                .pythonPath(pythonPath)
                .build();

        //Set the configuration of python to step
        PythonStep onnx_step = new PythonStep().step(python_config);

        //ServingConfig set httpport and Input Formats
        int port = Util.randInt(1000, 65535);
        ServingConfig servingConfig = ServingConfig.builder().httpPort(port).
                build();

        //Image Loading step for image transform
        ImageLoadingStep imageLoadingStep = ImageLoadingStep.builder()
                .inputName("inputimage")
                .dimensionsConfig("default", new Long[]{478L, 720L, 3L}) // Height, width, channels
                .build();

        //Inference Configuration
        InferenceConfiguration inferenceConfiguration = InferenceConfiguration.builder()
                .steps(Arrays.asList(imageLoadingStep, onnx_step)).servingConfig(servingConfig).build();

        //Print the configuration to make sure our settings correctly set.
        System.out.println(inferenceConfiguration.toJson());

        File configFile = new File("config.json");
        FileUtils.write(configFile, inferenceConfiguration.toJson(), Charset.defaultCharset());

        //Preparing input images.
        File imageOnnx = new ClassPathResource("data/facedetector/OnnxImageTest.jpg").getFile();

        //Start inference server as per the above configurations
        KonduitServingMain.builder()
                .onSuccess(() -> {
                    try {
                        String response = Unirest.post(String.format("http://localhost:%s/raw/image", port))
                                .field("inputimage", imageOnnx)
                                .asString().getBody();

                        System.out.println(response);
                        System.exit(0);
                    } catch (UnirestException e) {
                        e.printStackTrace();
                        System.exit(0);
                    }
                })
                .build()
                .runMain("--configPath", configFile.getAbsolutePath());
    }

}
