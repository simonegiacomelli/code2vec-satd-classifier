class ThrowsProblem {

    /**
     * Sends an HTTP GET request
     *
     * @param url to connect to
     * @return Response object containing status code and response body
     * @throws IOException when there is a problem establishing a connection
     */
    public static Response Get(String url) throws IOException {
        Response response = new Response();
        // Setup connection
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        conn.setRequestMethod(""GET"");
        conn.connect();
        // Get response from the server
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()), 8192);
        StringBuilder sb = new StringBuilder();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            sb.append(line);
            sb.append('\n');
        }
        if (sb.length() > 0) {
            // Remove extraneous CR/LF
            sb.deleteCharAt(sb.length() - 1);
        }
        // Store response in a Response object
        response.status = conn.getResponseCode();
        response.body = sb.toString();
        response.headers = conn.getHeaderFields();
        // Clean up
        conn.disconnect();
        return response;
    }

}