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


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;

/**
 * Example for client test of Inference for DataVec ML model using Model step .
 */
public class InferenceModelStepDataVecTest {
    public static void main(String[] args) throws Exception {
        //Preparing input NDArray
        HashMap<String, String> data_input = new HashMap<>();
        data_input.put("first", "value");


        // Create new JSON Object
        JsonObject person = new JsonObject();
        person.put("first", "last check");
        // person.addProperty("lastName", "Kargopolov");

        String jsonArrayString = "[\"value\"]";
        //  String jsonString = "{"first\":\"value\"}";

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:3000/raw/json")
                .header("Content-Type", "application/json")
                .body("{\"first\" :\"value\"}").asJson();
        //  .field("input",person).asJson();
        //.asString().getBody();

        System.out.print(response);
    }
}
