package bmt;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class App 
{
    static String httpRequest (String url) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET() // Default method
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() == 200)? response.body() : "";
        } catch (Exception e) {
            return "";
        }
    }

    static int getNumDraws(int year) {
        try {
            int count = 0;
            for (int x = 0; x < 11; x++) {
                String url = "https://jsonmock.hackerrank.com/api/football_matches"
                    + "?year=" + year  
                    + "&team1goals=" + x
                    + "&team2goals=" + x;
                String json = httpRequest(url);
                JSONObject jsonObject = new JSONObject(json);
                count += jsonObject.getInt("total");
            }
            return count;
        } catch (Exception e) {
            return -1;
        }
    }

    static int addScoreInData(JSONArray array, boolean isTeamOne) {
        int count = 0;
        String teamgoal = (isTeamOne)? "team1goals" : "team2goals";
        for (int i = 0; i < array.length(); i++) {
            System.out.println(array.getJSONObject(i));
            count += array.getJSONObject(i).getInt(teamgoal);
        }
        return count;
    }

    static int totalScoreInAPage(int page, String team, String competition, boolean isTeamOne, int year) {
        try {
            String url = "https://jsonmock.hackerrank.com/api/football_matches"
                + "?competition=" + URLEncoder.encode(competition, "UTF-8") 
                + "&page=" + page
                + "&year=" + year;
            url = (isTeamOne == true)? url + "&team1=": url + "&team2=";
            url = url + URLEncoder.encode(team, "UTF-8");
            String json = httpRequest(url);
            JSONObject jsonObject = new JSONObject(json);
            return addScoreInData(jsonObject.getJSONArray("data"), isTeamOne);
        } catch (Exception e) {
            return -1;
        }
    }

    static int getTotalScoreInTeamOneOrTwo(String team, String competition, boolean isTeamOne, int year) {
        try {
            String url = "https://jsonmock.hackerrank.com/api/football_matches"
            + "?competition=" + URLEncoder.encode(competition, "UTF-8") 
            + "&page=" + 1
            + "&year=" + year;
            url = (isTeamOne == true) ? url + "&team1=" : url + "&team2=";
            url = url +  URLEncoder.encode(team, "UTF-8");
            String json = httpRequest(url);
            JSONObject jsonObject = new JSONObject(json);
            int total = jsonObject.getInt("total");
            int per_page = jsonObject.getInt("per_page");
            int count = 0;
            count += addScoreInData(jsonObject.getJSONArray("data"), isTeamOne);
            int total_page = (total % per_page == 0) ? total | per_page : total | per_page + 1;
            for (int page = 2; page <= total_page; page++) {
                count += totalScoreInAPage(page, team, competition, isTeamOne, year);
            }
            return count;
        } catch (Exception e) {
            return -1;
        }
    }

    static String getWinner(String competition, int year) {
        try {
             String url = "https://jsonmock.hackerrank.com/api/football_competitions"
            + "?year=" + year  
            + "&name=" + URLEncoder.encode(competition, "UTF-8");
            String json = httpRequest(url);
            JSONObject jsonObject = new JSONObject(json);
            String winner = jsonObject.getJSONArray("data").getJSONObject(0).getString("winner");
            return winner;
        } catch (Exception e) {
            return "";
        }
    }

    static int getTeamTotalGoals(String competition, int year, String team) {
        boolean isTeamOne = true;
        int count = getTotalScoreInTeamOneOrTwo(team, competition, isTeamOne, year);
        isTeamOne = false;
        count += getTotalScoreInTeamOneOrTwo(team, competition, isTeamOne, year);
        return count;
    }

    static int getWinnerTotalGoals(String competition, int year) {
        try {
            String team = getWinner(competition, year);
            System.out.println("Winning team in " + year + ": " + team);
            return getTeamTotalGoals(competition, year, team);
        } catch (Exception e) {
            return -1;
        }
    }

    public static void main( String[] args )
    {
        try {
            System.out.println("Number of draws in 2011: " + getNumDraws(2011));
            String output = "Total goals in 2011 by winning team for UEFA Champions League is: " 
                + getWinnerTotalGoals("UEFA Champions League", 2011);
            System.out.println(output);
        } catch (Exception e) {
        }
       
    }
}
