package Hubo.com;

import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;

import java.util.ArrayList;
import java.net.*;
import java.io.*;
import java.util.Iterator;

import org.json.simple.*;
/**
 * Created by richard on 28/07/2016.
 */

// DATa:
    /*
    http://data.nantes.fr/api/publication/24440040400129_NM_NM_00184/APPUIS_VELOS_NM_STBL/content/?format=json
     */
public class OpenDataManager {
    public ArrayList<Marker> getListeMarker() {
        ArrayList res = new ArrayList<Marker>();


        MarkerOptions markerOptions2 = new MarkerOptions();
        LatLong markerLatLong2 = new LatLong(47.24816, -1.57874);
        markerOptions2.position(markerLatLong2)
                .title("My new Marker")
                .visible(true);

        Marker myMarker2 = new Marker(markerOptions2);

        res.add(myMarker2);


        try {
            String url_select = "    http://data.nantes.fr/api/publication/24440040400129_NM_NM_00184/APPUIS_VELOS_NM_STBL/content/?format=json";


            URL url = new URL(url_select);
            StringBuilder postData = new StringBuilder();
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");


            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);


            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null; ) {
                builder.append(line).append("\n");
            }
            reader.close();
            conn.disconnect();
            Object obj = JSONValue.parse(builder.toString());
            JSONArray dataTable = (JSONArray) ((JSONObject)obj).get("data");
            Iterator I= dataTable.iterator();
            while(I.hasNext()){
                JSONObject crt = (JSONObject) I.next();

                JSONArray pos= (JSONArray) crt.get("_l");
                System.out.println(pos);


                MarkerOptions markerOptions = new MarkerOptions();
                double lat,lon;
                lat = (double) pos.get(0);
                lon = (double) pos.get(1);
                LatLong markerLatLong = new LatLong(lat, lon);
                markerOptions.position(markerLatLong)
                        .title("My new Marker")
                        .visible(true);

                Marker myMarker = new Marker(markerOptions);

                res.add(myMarker);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

}
