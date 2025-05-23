/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.forms

import java.io.Serializable

data class MediaFile @JvmOverloads constructor(
    val filename: String,
    val hash: String,
    val downloadUrl: String,
    val type: Type? = null,
    val integrityUrl: String? = null
) : Serializable {

    enum class Type {
        ENTITY_LIST,
        APPROVAL_ENTITY_LIST
    }
}
