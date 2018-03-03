package com.example.VertxApp;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MainVerticle extends AbstractVerticle {

    HttpServer httpServer;
    Router router;
    HttpClient httpClient;
    HandlebarsTemplateEngine engine;
    SQLConnection connection;
    String encodedImage = "", encodedAudio = "", encodedVideo = "", name = "", phone = "", email = "", question = "";
    String nameData = "", phoneData = "", emailData = "", questionData = "", imageData = "", audioData = "";
    List<UserTest> finalArray, formArray;

    @Override
    public void start() throws Exception {

        httpServer = vertx.createHttpServer();
        router = Router.router(vertx);
        httpClient = vertx.createHttpClient();
        engine = HandlebarsTemplateEngine.create();

        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));

        router.route("/gagan/garg").handler(routingContext -> {

            HttpServerResponse serverResponse = routingContext.response();

            final String url = "****************";
            httpClient.getAbs(url, response -> {

                if (response.statusCode() != 200) {
                    System.err.println("fail");
                } else {

                    response.bodyHandler(res1 -> {
                        serverResponse.putHeader("content-type", "application/json").end(res1);
                    });

                    engine.render(routingContext, "templates/index.hbs", res -> {
                        if (res.succeeded()) {
                            serverResponse.setChunked(true);
                            serverResponse.write(res.result());
                            routingContext.put("body", response.bodyHandler(res1 -> {
                                serverResponse.putHeader("content-type", "application/json").end(res1);
                            }));
                        } else {
                            routingContext.fail(res.cause());
                        }
                    });
                }

            }).end();

        });

        router.get("/template/handler").handler(ctx -> {

            engine.render(ctx, "templates/index.hbs", res -> {
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });

        });

        router.get("/template/submit_qaform").handler(ctx -> {

            ctx.put("name", "GAGAN");
            engine.render(ctx, "templates/submit_qa.hbs", res -> {
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });

        });


        router.get("/template/user_details").handler(ctx -> {

            engine.render(ctx, "templates/user_detail.hbs", res -> {
                if (res.succeeded()) {
                    ctx.response().end(res.result());
                } else {
                    ctx.fail(res.cause());
                }
            });

        });


        router.post("/abc").handler(ctx -> {

            HttpServerResponse serverResponse = ctx.response();
            HttpServerRequest hr = ctx.request();
            name = hr.params().get("firstname");
            phone = hr.params().get("phone");
            email = hr.params().get("email");
            question = hr.params().get("question");

            for (FileUpload fu : ctx.fileUploads()) {

                if (fu.fileName().contains("jpg") || fu.fileName().contains("png") || fu.fileName().contains("jpeg")) {
                    try {
                        File f = new File(fu.uploadedFileName());
                        BufferedImage image = ImageIO.read(f);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, "png", baos);
                        byte[] res = baos.toByteArray();
                        encodedImage = Base64.encode(baos.toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (fu.fileName().contains("mp3")) {

                    try {
                        File audioFile = new File(fu.uploadedFileName());
                        byte[] bytesArray = new byte[(int) audioFile.length()];
                        FileInputStream fis = null;
                        fis = new FileInputStream(audioFile);
                        fis.read(bytesArray); //read file into bytes[]
                        fis.close();
                        encodedAudio = Base64.encode(bytesArray);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (fu.fileName().contains("mp4")) {
                    try {
                        File videoFile = new File(fu.uploadedFileName());
                        byte[] bytesArray = new byte[(int) videoFile.length()];
                        FileInputStream fis = null;
                        fis = new FileInputStream(videoFile);
                        fis.read(bytesArray); //read file into bytes[]
                        fis.close();
                        encodedVideo = Base64.encode(bytesArray);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            if (!name.equalsIgnoreCase("")) {
                if (!phone.equalsIgnoreCase("")) {
                    if (!email.equalsIgnoreCase("")) {
                        if (!question.equalsIgnoreCase("")) {
                            if (!encodedImage.equalsIgnoreCase("")) {
                                if (!encodedAudio.equalsIgnoreCase("")) {
                                    //  if (!encodedVideo.equalsIgnoreCase("")) {
                                    //formArray = ;
                                    saveInDatabase(ctx, name, phone, email, question, encodedImage, encodedAudio);


                                    //  }
                                }
                            }
                        }
                    }
                }
            }


        });

        httpServer.requestHandler(router::accept).listen(5200);

    }

    private void saveInDatabase(RoutingContext ctx, String name, String phone, String email, String
        question, String imageFile, String audioFile) {
        JsonObject mySQLClientConfig = new JsonObject()
            .put("host", "@@@@@@@@@@@")
            .put("username", "@@@@@@@@@@@@")
            .put("password", "@@@@@@@@@@@")
            .put("database", "@@@@@@@@@@@@@@")
            .put("charset", "@#@@@@@@@@@@@");
        //   jsonArray = new ArrayList<JsonArray>();
        finalArray = new ArrayList<UserTest>();
        SQLClient mySQLClient = MySQLClient.createShared(vertx, mySQLClientConfig);
        mySQLClient.getConnection(res -> {
            if (res.succeeded()) {
                connection = res.result();
                connection.execute("insert into tbl_vertx_demo (name,email,phone,question,image,audio) values ('" + name + "', '" + email + "','" + phone + "','" + question + "','" + imageFile + "','" + audioFile + "')", r -> {
                    if (r.succeeded()) {
                        connection.query("select name,email,phone,question from tbl_vertx_demo", show -> {
                            for (JsonArray jsonObj : show.result().getResults()) {

                                finalArray.add(
                                    UserTest
                                        .builder()
                                        .name(jsonObj.getString(0))
                                        .email(jsonObj.getString(1))
                                        .phone(jsonObj.getString(2))
                                        .question(jsonObj.getString(3))
                                        .build());

                            }

                            ctx.put("UserList", finalArray);
                            engine.render(ctx, "templates/user_detail.hbs", res1 -> {
                                if (res1.succeeded()) {
                                    ctx.response().end(res1.result());
                                } else {
                                    ctx.fail(res1.cause());
                                }
                            });

                            // response.putHeader("Content-Type", "application/json").end(rs.getResults().toString());
                            //  test.addAll(rs.getResults());
                        });
                    } else {
                        ctx.response().end(String.valueOf(r.result()));
                    }
                });
            } else {
            }
        });

    }

}
