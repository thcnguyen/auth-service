package exchange.velox.authservice.authservice.rest;

import exchange.velox.authservice.authservice.domain.UserDTO;
import exchange.velox.authservice.authservice.domain.UserSessionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public interface UserAPI {

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    UserSessionDTO login(@RequestHeader("Authorization") String authorization);

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization);

    @RequestMapping(value = "/token", method = RequestMethod.GET)
    ResponseEntity<?> checkValidToken(@RequestHeader("Authorization") String authorization);

    @RequestMapping(value = "/token/forgotPassword", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void forgotPassword(@RequestBody Map<String, String> data);

    @RequestMapping(value = "/token/forgotPassword", method = RequestMethod.PUT)
    UserDTO updateForgottenPassword(@RequestBody Map<String, String> data);
}
