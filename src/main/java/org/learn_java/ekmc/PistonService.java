package org.learn_java.ekmc;

import org.learn_java.ekmc.model.RunRequest;
import org.learn_java.ekmc.model.RunResponse;
import org.learn_java.ekmc.model.Language;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import java.util.List;

public interface PistonService {
    @GET("piston/runtimes")
    Call<List<Language>> getRuntimes();

    @POST("piston/execute")
    Call<RunResponse> execute(@Body RunRequest request);
}
