/*
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Entry point of the Community service (container crapi-community, Go).
//
// Everything of substance lives in the api package; this just calls api.Run,
// which connects to Postgres and Mongo, waits for crapi-identity to be healthy,
// seeds the forum data and starts the HTTP router on SERVER_PORT (8087).
package main

import (
	"crapi.proj/goservice/api"
)

func main() {
	api.Run()
}
