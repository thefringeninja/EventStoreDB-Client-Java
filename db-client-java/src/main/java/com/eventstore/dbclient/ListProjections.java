package com.eventstore.dbclient;

import com.eventstore.dbclient.proto.projections.Projectionmanagement;
import com.eventstore.dbclient.proto.projections.ProjectionsGrpc;
import com.eventstore.dbclient.proto.shared.Shared;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


public class ListProjections {
    private final GrpcClient client;
    private final Metadata metadata;

    public ListProjections(final GrpcClient client, final ListProjectionsOptions options) {
        this.client = client;
        this.metadata = options.getMetadata();
    }

    public CompletableFuture<ListProjectionsResult> execute() {
        return this.client.run(channel -> {
            Projectionmanagement.StatisticsReq.Options.Builder optionsBuilder =
                Projectionmanagement.StatisticsReq.Options.newBuilder()
                    .setContinuous(Shared.Empty.newBuilder());

            Projectionmanagement.StatisticsReq request = Projectionmanagement.StatisticsReq.newBuilder()
                .setOptions(optionsBuilder)
                .build();

            ProjectionsGrpc.ProjectionsStub client = MetadataUtils
                .attachHeaders(ProjectionsGrpc.newStub(channel), this.metadata);

            CompletableFuture<ListProjectionsResult> future = new CompletableFuture<>();
            ArrayList<ProjectionDetails> projections = new ArrayList<>();

            client.statistics(request, new StreamObserver<Projectionmanagement.StatisticsResp>() {
                @Override
                public void onNext(Projectionmanagement.StatisticsResp value) {
                    if (value.hasDetails()) {
                        projections.add(ProjectionDetails.fromWire(value.getDetails()));
                    }
                }

                @Override
                public void onCompleted() {
                    future.complete(new ListProjectionsResult(projections));
                }

                @Override
                public void onError(Throwable t) {
                    future.completeExceptionally(t);
                }
            });

            return future;
        });
    }
}
