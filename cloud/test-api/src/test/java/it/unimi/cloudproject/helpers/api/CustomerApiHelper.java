package it.unimi.cloudproject.helpers.api;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import it.unimi.cloudproject.api.callers.CustomerApiCaller;
import it.unimi.cloudproject.helpers.dto.NewUserInfo;
import it.unimi.cloudproject.lambda.customer.dto.requests.user.UserCreationRequest;
import it.unimi.cloudproject.lambda.customer.dto.requests.user.UserLoginRequest;
import it.unimi.cloudproject.lambda.customer.dto.responses.LoginResponse;
import it.unimi.cloudproject.lambda.customer.dto.responses.UserCreationResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerApiHelper {
    /**
     * Create a user and login with its credentials
     * @param customerApiCaller
     * @param userCreationRequest
     * @return the idToken and the id of the new user
     */
    public static NewUserInfo createUserAndLoginSuccessfully(CustomerApiCaller customerApiCaller,
                                                             UserCreationRequest userCreationRequest) throws IOException, InterruptedException {
        var userCreateResponse = customerApiCaller.<UserCreationResponse>callUserCreateApi(userCreationRequest);
        var id = userCreateResponse.body().id();
        assertThat(userCreateResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(id).isGreaterThanOrEqualTo(1);

        // login
        var userLoginResponse = customerApiCaller.<LoginResponse>callUserLoginApi(
                new UserLoginRequest(userCreationRequest.username(), userCreationRequest.password()));
        assertThat(userLoginResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        var idToken = userLoginResponse.body().idToken();
        assertThat(idToken).isNotEmpty();

        return new NewUserInfo(id, idToken);
    }
}
