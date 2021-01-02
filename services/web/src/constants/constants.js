/*
 * Copyright 2020 Traceable, Inc.
 *
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

// Validation regexp
export const NUMBERSVALIDATION = /^\d+$/;
export const MATCH_ANYTHING = /.*?/;
export const EMAIL_VALIDATION = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
export const PHONE_VALIDATION = /^\(?([0-9-. ]*)\)?([0-9-. ]*)$/;
export const PASSWORD_VALIDATION =/^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,16}$/;
export const NAME_VALIDATION = /^[a-zA-Z ]+$/;
export const PIN_CODE_VALIDATION = /^[0-9]{4}$/;
export const VIN_VALIDATION = /^[0-9][A-Z]{4}[0-9]{2}[A-Z]{4}[0-9]{6}$/;
