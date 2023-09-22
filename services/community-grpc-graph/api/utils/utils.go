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

package utils

import (
	"os"
	"strings"
)

func IsTrue(a string) bool {
	a = strings.ToLower(a)
	true_list := []string{"true", "1"}
	for _, b := range true_list {
		if b == a {
			return true
		}
	}
	return false
}

func IsTLSEnabled() bool {
	tls_enabled, is_tls := os.LookupEnv("TLS_ENABLED")
	if is_tls && IsTrue(tls_enabled) {
		return true
	}
	return false
}
