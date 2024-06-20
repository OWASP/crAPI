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

package com.crapi.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.crapi.constant.UserMessage;
import com.crapi.model.CRAPIResponse;
import com.crapi.model.ErrorDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
@RestController
public class ExceptionHandler extends ResponseEntityExceptionHandler {

  private static final long serialVersionUID = -3880069851908752573L;

  @org.springframework.web.bind.annotation.ExceptionHandler({RuntimeException.class})
  public ResponseEntity<String> handleRunTimeException(RuntimeException e) {
    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(e.getMessage());
  }

  @org.springframework.web.bind.annotation.ExceptionHandler({CRAPIExceptionHandler.class})
  private ResponseEntity<Object> handleCRAPIException(CRAPIExceptionHandler e) {
    CRAPIResponse cr = new CRAPIResponse(e.getMessage(), e.getStatus());
    return new ResponseEntity<Object>(cr, HttpStatus.valueOf(e.getStatus()));
  }

  @org.springframework.web.bind.annotation.ExceptionHandler({LockedException.class})
  private ResponseEntity<Object> handleAccountLockException(LockedException e) {
    // Print stack trace
    e.printStackTrace();
    CRAPIResponse cr =
        new CRAPIResponse(UserMessage.ACCOUNT_LOCKED_MESSAGE, HttpStatus.LOCKED.value());
    return new ResponseEntity<Object>(cr, HttpStatus.LOCKED);
  }

  // @org.springframework.web.bind.annotation.ExceptionHandler({MethodArgumentNotValidException.class})
  // public ResponseEntity<String> handleMethodArgumentNotValid(
  //     MethodArgumentNotValidException ex,
  //     HttpHeaders headers,
  //     HttpStatus status,
  //     WebRequest request) {
  //   // ErrorDetails errorDetails =
  //   //    new ErrorDetails(, );
  //   return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("Validation Failed");
  // }

  @Override
  @ResponseBody
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String bindingResult = ex.getBindingResult().toString();
    ErrorDetails errorDetails = new ErrorDetails("Validation failed", bindingResult);
    return new ResponseEntity<Object>(errorDetails, HttpStatus.BAD_REQUEST);
  }
}
