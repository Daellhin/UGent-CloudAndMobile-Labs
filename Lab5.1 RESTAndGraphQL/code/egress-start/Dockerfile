# Build time image
FROM gradle:8.6.0-jdk17-alpine AS BUILD_ENV
COPY ./ /egress
RUN gradle buildFatJar -p /egress

# Runtime image
FROM gcr.io/distroless/java17-debian12
LABEL authors="Tom Windels"
COPY --from=BUILD_ENV /egress/build/libs/egress.jar .
CMD ["egress.jar"]
EXPOSE 8087
