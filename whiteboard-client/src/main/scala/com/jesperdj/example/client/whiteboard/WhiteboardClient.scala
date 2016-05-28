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

import java.util.Date

import com.jesperdj.example.client.whiteboard.domain.Note
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.hateoas.Resources
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}

import scala.collection.JavaConversions._

@FeignClient(name = "whiteboard-service", fallback = classOf[WhiteboardClientFallback])
trait WhiteboardClient {

  @RequestMapping(method = Array(RequestMethod.GET), path = Array("/notes"))
  def getAllNotes: Resources[Note]

  @RequestMapping(method = Array(RequestMethod.POST), path = Array("/notes"))
  def addNote(note: Note): Note
}

@Component
class WhiteboardClientFallback extends WhiteboardClient {

  override def getAllNotes: Resources[Note] = {
    val note = new Note
    note.createdDateTime = new Date()
    note.authorName = "System"
    note.content = "The whiteboard is currently not available."
    new Resources[Note](Seq(note), Seq())
  }

  override def addNote(note: Note): Note = note // Do nothing
}
