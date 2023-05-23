package model;

import controller.Server;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

public class SyntaxParser {    
    private static final String PATRON_METODO = "(OPTIONS|GET|HEAD|POST|PUT|DELETE|TRACE|CONNECT)";
    private static final String PATRON_ESQUEMA = "(http|https)";
    private static final String PATRON_HOST = "([A-Za-z0-9_+-./:=?&%;]+)+";
    private static final String PATRON_PROTOCOLO = "([A-Za-z0-9_+-./:=?&%;]+)+";
    private static final String PATRON_GENERICO = ".+";
    private static final String NOMBRE_CABEZA =  "[A-Za-z0-9_+-./=?&%;]+"; //"[A-Za-z_+-]+";
    private static final String VALOR_CABEZA = "[A-Za-z0-9_+-./:=?&%;]+"; //"[A-Za-z0-9_+-./=?&%;]+";
    private static final String JSON = "\\{\\s*\"(\\w+)\":\\s*(\"[^\"]*\"|\\d+|\\{[^{}]*\\}|\\[[^\\[\\]]*\\])\\s*(,\\s*\"(\\w+)\":\\s*(\"[^\"]*\"|\\d+|\\{[^{}]*\\}|\\[[^\\[\\]]*\\])\\s*)*\\}";
    
    private String metodo;
    private String protocolo;
    
    Server request = new Server();
    
    public String getMetodo(){
        return metodo;
    }

    public void setMetodo(){
        this.metodo = metodo;
    }
    
    public String getProtocolo(){
        return protocolo;
    }

    public void setProtocolo(){
        this.protocolo = protocolo;
    }
    
    public static Map<String, Object> parseJSON(String json) {

        json = json.replaceAll("[{}\"]", "");


        String[] keyValuePairs = json.split(",");


        Map<String, Object> variables = new HashMap<>();


        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split(":");

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            variables.put(key, value);

        }
        return variables;
    }
    
    public void ApiRest(String linea, String cabecera, String body, int id) {
        String lineaOutput = "";
        String cabeceraOutput = "";
        String bodyOutput = "";
        Boolean parserOk = false;



        Pattern pattern = Pattern.compile(
            String.format("%s %s://%s %s(\\r\\n)?(\\n)?", PATRON_METODO, PATRON_ESQUEMA, PATRON_HOST, PATRON_PROTOCOLO),Pattern.MULTILINE
        );                   

        Matcher matcher = pattern.matcher(linea);

        
        if (matcher.matches()) {
            

            

            metodo = matcher.group(1);

            String esquema = matcher.group(2);

            String servidor = matcher.group(3);

            protocolo = matcher.group(4);
                        

            

            pattern = Pattern.compile(String.format("(%s): (%s)(\\r\\n)?(\\n)?", NOMBRE_CABEZA, VALOR_CABEZA));
            matcher = pattern.matcher(cabecera);

            
            int contador = 0;
            while (matcher.find()) {
                contador = contador + 1;
                String headerName = matcher.group(1);
                String headerValue = matcher.group(2);
            }

            int lineas = cabecera.split("\n").length;

            
            if (lineas == contador){

                

                if(!body.isEmpty()){
                    pattern = Pattern.compile(
                    String.format("%s(\\r\\n)?(\\n)?", JSON),Pattern.MULTILINE);
                
                    matcher = pattern.matcher(body);
                    
                    if (matcher.matches()) {

                        parserOk = true;
                        lineaOutput = protocolo + " 200 OK";
                    }
                    else{
                        lineaOutput = "400 BAD REQUEST\n";
                        cabeceraOutput = "PARSER-ERROR: Cuerpo de peticion no valido. Json incorrecto" + body;
                    }
                }
                else {
                    parserOk = true;
                    lineaOutput = protocolo + " 200 OK";
                }
            }
            else {
                lineaOutput = "400 BAD REQUEST\n";
                cabeceraOutput = "PARSER-ERROR: Cabecera de peticion incorrecta. Lineas cabecera incorrectas: " + (lineas-contador);
            }
        } 
        else {

            pattern = Pattern.compile(String.format("%s%s(\\r\\n)?(\\n)?", PATRON_METODO, PATRON_GENERICO));
            matcher = pattern.matcher(linea);    
            if (matcher.matches()) {

                pattern = Pattern.compile(String.format("%s %s%s(\\r\\n)?(\\n)?", PATRON_METODO, PATRON_ESQUEMA, PATRON_GENERICO));
                matcher = pattern.matcher(linea);    
                if (matcher.matches()) {

                    pattern = Pattern.compile(String.format("%s %s://%s%s(\\r\\n)?(\\n)?", PATRON_METODO, PATRON_ESQUEMA, PATRON_HOST, PATRON_GENERICO));
                    matcher = pattern.matcher(linea);    
                    if (matcher.matches()) {

                        pattern = Pattern.compile(String.format("%s %s://%s %s%s(\\r\\n)?(\\n)?", PATRON_METODO, PATRON_ESQUEMA, PATRON_HOST, PATRON_PROTOCOLO, PATRON_GENERICO));
                        matcher = pattern.matcher(linea);    
                        if (matcher.matches()) {

                        }
                        else{
                            lineaOutput = "400 BAD REQUEST\n";
                            cabeceraOutput = "PARSER-ERROR: El protocolo no esta en la linea de peticion \n";
                        }
                    }
                    else{
                        lineaOutput = "400 BAD REQUEST\n";
                        cabeceraOutput = "PARSER-ERROR: El servidor no fue incluido en la linea de peticion \n";
                    }
                }
                else{
                    lineaOutput = "400 BAD REQUEST\n";
                    cabeceraOutput = "PARSER-ERROR: El esquema (http|https) no se incluyo o fue colocado de manera incorrecta en la linea de peticion \n";
                }
            }
            else{
                lineaOutput = "400 BAD REQUEST\n";
                cabeceraOutput = "PARSER-ERROR: El metodo (OPTIONS|GET|HEAD|POST|PUT|DELETE|TRACE|CONNECT) no se incluyo en la linea de peticion \n"; 
            }

            lineaOutput = "400 BAD REQUESTttt\n";
        }
        if (parserOk){
            switch (metodo) {
                case "GET" -> {
                    if (id == 0) {

                        request = new Server("GET", "http://localhost:8080/api/users", "Content-Type: application/json", "");

                    } else {
                        System.out.println(lineaOutput);
                        request = new Server("GET", "http://localhost:8080/api/users", "Content-Type: application/json", "", id);
                    }
                }
                case "POST" -> {
                    lineaOutput = protocolo + " 201 CREATED";
                    System.out.println(lineaOutput);
                    Map<String, Object> variables = parseJSON(body);
                    String usuario = variables.get("nombres") + "," + variables.get("email") + "," + variables.get("phone");
                    request = new Server("POST", "http://localhost:8080/api/users", "Content-Type: application/json", usuario);
                }
                case "PUT" -> {
                    lineaOutput = protocolo + " 201 CREATED";
                    System.out.println(lineaOutput);
                    Map<String, Object> variables1 = parseJSON(body);
                    String usuario1 = variables1.get("nombres") + "," + variables1.get("email") + "," + variables1.get("phone");
                    request = new Server("PUT", "http://localhost:8080/api/users", "Content-Type: application/json", usuario1, id);
                }
                case "DELETE" -> {
                    System.out.println(lineaOutput);
                    request = new Server("DELETE", "http://localhost:8080/api/users", "Content-Type: application/json", "", id);
                }
                default -> System.out.println("Método no soportado");
            }
        }
        else {
            System.out.println(lineaOutput + cabeceraOutput + bodyOutput);
        }
    }
}
