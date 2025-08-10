import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class httpServer {
    public static void main(String[] args) throws IOException {
        SetHttpServer();
    }
    public static void SetHttpServer() throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080),0);
        
        server.createContext("/API", new APIHandler());
        server.createContext("/", new StaticFileHandler("../web"));
        server.createContext("/status", exchange -> {
            String response = "Java backend is running";
            exchange.sendResponseHeaders(200,response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://localhost:8080");
    }

    static class StaticFileHandler implements HttpHandler{

        private final String baseDir;

        StaticFileHandler(String baseDir){
            this.baseDir = baseDir;
        }


        @Override
        public void handle(HttpExchange exchange) throws IOException {
            
            try {

                String path = exchange.getRequestURI().getPath();

                if (path.equals("/")){
                    path = "/index.html";
                }

                Path filepth = Path.of(baseDir,path);

                if (Files.exists(filepth) && !Files.isDirectory(filepth)){

                    String Content_type = Files.probeContentType(filepth);
                    if (Content_type == null){
                        Content_type = "application/octet-stream";
                    }
                    exchange.getResponseHeaders().set("Content-type", Content_type);

                    byte[] bytes = Files.readAllBytes(filepth);
                    exchange.sendResponseHeaders(200, bytes.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }

                } else {
                        String notFound = "404 Not Found";
                        exchange.sendResponseHeaders(404, notFound.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(notFound.getBytes());
                        }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                String errorMsg = "500 Internal Server Error";
                exchange.sendResponseHeaders(500, errorMsg.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errorMsg.getBytes());
                }
            }
        }  

    }

    static class APIHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException{
            
            String path = exchange.getRequestURI().getPath();

            switch (path) {
                case "API/Recive":
                    
                    

                    break;
                
                case "API/Send":

                    break;

                default:
                    break;
            }
        }

        
    }

}
