package exchange.velox.authservice.rest;

import exchange.velox.authservice.dto.UserSessionDTO;
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
    UserSessionDTO checkValidToken(@RequestHeader("Authorization") String authorization);

    @RequestMapping(value = "/token/forgotPassword", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void forgotPassword(@RequestBody Map<String, String> data);

    @RequestMapping(value = "/token/inviteUser", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void inviteUser(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> data);

    @RequestMapping(value = "/token/forgotPassword", method = RequestMethod.PUT)
    UserSessionDTO updateForgottenPassword(@RequestBody Map<String, String> data);
}
