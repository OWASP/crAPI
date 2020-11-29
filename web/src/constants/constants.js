// Validation regexp
export const NUMBERSVALIDATION = /^\d+$/;
export const MATCH_ANYTHING = /.*?/;
export const EMAIL_VALIDATION = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
export const PHONE_VALIDATION = /^\(?([0-9-. ]*)\)?([0-9-. ]*)$/;
export const PASSWORD_VALIDATION =/^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,16}$/;
export const NAME_VALIDATION = /^[a-zA-Z ]+$/;
export const PIN_CODE_VALIDATION = /^[0-9]{4}$/;
export const VIN_VALIDATION = /^[0-9][A-Z]{4}[0-9]{2}[A-Z]{4}[0-9]{6}$/;
