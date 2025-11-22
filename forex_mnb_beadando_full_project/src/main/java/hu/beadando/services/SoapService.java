package hu.beadando.services;

import hu.beadando.models.Rate;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Service
public class SoapService {

    private static final String MNB_URL = "https://www.mnb.hu/arfolyamok.asmx";

    public List<Rate> getRates(String currency, LocalDate start, LocalDate end) throws Exception {
        String requestXml = buildRequest(currency, start, end);
        String responseXml = callSoapService(requestXml);
        return parseRatesFromResponse(responseXml, currency);
    }

    private String buildRequest(String currency, LocalDate start, LocalDate end) {
        String startStr = start.toString();
        String endStr = end.toString();

        return ""
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:mnb=\"http://www.mnb.hu/webservices/\">"
                + "<soap:Body>"
                + "<mnb:GetExchangeRates>"
                + "<mnb:startDate>" + startStr + "</mnb:startDate>"
                + "<mnb:endDate>" + endStr + "</mnb:endDate>"
                + "<mnb:currencyNames>" + currency + "</mnb:currencyNames>"
                + "</mnb:GetExchangeRates>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    private String callSoapService(String requestXml) throws Exception {
        URL url = new URL(MNB_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        conn.setRequestProperty("SOAPAction", "http://www.mnb.hu/webservices/GetExchangeRates");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestXml.getBytes(StandardCharsets.UTF_8));
        }

        InputStream is;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }

        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = is.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private List<Rate> parseRatesFromResponse(String soapResponse, String currency) throws Exception {
        List<Rate> result = new ArrayList<>();

        int startIndex = soapResponse.indexOf("<GetExchangeRatesResult>");
        int endIndex = soapResponse.indexOf("</GetExchangeRatesResult>");
        if (startIndex == -1 || endIndex == -1) {
            return result;
        }

        String innerXml = soapResponse.substring(startIndex + "<GetExchangeRatesResult>".length(), endIndex);
        innerXml = innerXml.replace("&lt;", "<").replace("&gt;", ">");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(innerXml.getBytes(StandardCharsets.UTF_8)));

        NodeList dayNodes = doc.getElementsByTagName("Day");
        for (int i = 0; i < dayNodes.getLength(); i++) {
            Element dayEl = (Element) dayNodes.item(i);
            String dateStr = dayEl.getAttribute("date");
            LocalDate date = LocalDate.parse(dateStr);

            NodeList rateNodes = dayEl.getElementsByTagName("Rate");
            for (int j = 0; j < rateNodes.getLength(); j++) {
                Element rateEl = (Element) rateNodes.item(j);
                String curr = rateEl.getAttribute("curr");
                if (!currency.equalsIgnoreCase(curr)) {
                    continue;
                }
                String valueStr = rateEl.getTextContent().trim().replace(",", ".");
                BigDecimal value = new BigDecimal(valueStr);
                Rate rate = new Rate();
                rate.setCurrency(curr);
                rate.setDate(date);
                rate.setValue(value);
                result.add(rate);
            }
        }

        return result;
    }
}
