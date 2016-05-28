/*
 * Copyright 2016 Jesper de Jong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jesperdj.example.client.whiteboard

import com.jesperdj.example.client.whiteboard.domain.Note
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.core.ParameterizedTypeReference
import org.springframework.hateoas.Resources
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.{RequestBody, RequestMapping, RequestMethod, ResponseBody}
import org.springframework.web.client.RestTemplate

import scala.collection.JavaConversions._

@Controller
class WhiteboardClientController @Autowired()(discoveryClient: DiscoveryClient, restTemplate: RestTemplate) {

  private def lookupWhiteboardService: ServiceInstance = {
    // Lookup instances of the service; return the first one
    val instances = discoveryClient.getInstances("whiteboard-service")
    instances(0)
  }

  @RequestMapping(Array("/"))
  def index(model: Model): String = {
    // Resolve URI collection of notes
    val uri = lookupWhiteboardService.getUri.resolve("/notes")

    // Call the service using the RestTemplate
    val typeRef = new ParameterizedTypeReference[Resources[Note]] {}
    val entity = restTemplate.exchange(uri, HttpMethod.GET, null, typeRef)

    // Store list of notes in the model
    model.addAttribute("notes", entity.getBody.getContent)

    // Return view name
    "index"
  }

  @RequestMapping(method = Array(RequestMethod.POST), path = Array("/add"), consumes = Array("application/json"))
  @ResponseBody
  def add(@RequestBody note: Note): Note = {
    // Resolve URI collection of notes
    val uri = lookupWhiteboardService.getUri.resolve("/notes")

    // Do a POST to the whiteboard service to create the new note
    restTemplate.postForObject(uri, note, classOf[Note])
  }
}
