import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.util.regex.*;

public class T1 {

    public static class Person {
        public int id;
        public int sex;
        public int country;
        public String name;
        public String birth;
        public String begin, end;

        Person() {
        }

        Person(String s) {
            name = s;
            birth = "";
        }

        public void InsertName(String s) {
            name = s;
        }

        public void InsertId(int t) {
            id = t;
        }

        public void InsertBirth(String s) {
            birth = s;
        }

        public void InsertSex(char c) {
            if (c == '♂') sex = 1;
            else sex = 2;
        }

        public void InsertCountry(int t) {
            country = t;
        }
    }

    public static Person A[];

    //获取url 返回字符串 解决中文乱码问题
    public static String Gget(String url1) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String context = "";
        try {
            URL url = new URL(url1);
            URI uri = new URI(url.getProtocol(), url.getHost() + ":" + url.getPort(), url.getPath(), url.getQuery(), null);
            // 创建httpget.
            HttpGet httpget = new HttpGet(uri);
            // System.out.println("executing request " + httpget.getURI());
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            // 获取响应实体
            HttpEntity entity = response.getEntity();
            // System.out.println("--------------------------------------");
            // 打印响应状态
            System.out.println(response.getStatusLine());
            if (entity != null) {
                // 打印响应内容 ，转换为utf-8格式，避免所传内容包含汉字乱码
                context = EntityUtils.toString(entity, "UTF-8");
                // System.out.println(context);
            }
            response.close();
            return context;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 正则匹配
    public static Vector<String> GetMatch(String w, String rex) {
        Pattern p = Pattern.compile(rex);
        Matcher m = p.matcher(w);
        Vector<String> ans = new Vector<String>();
        while (m.find()) ans.add(m.group(0));
        return ans;
    }

    public static void GetPlayersNameBirth(int l, int r) throws IOException {
        String s1 = "https://www.goratings.org/zh/players/", s2 = ".html";
        for (int i = l; i <= r; ++i) {
            System.out.print(i + " ");
            String w = Gget(s1 + Integer.toString(i) + s2);
            String rex = "";
            rex = "<title>[\\u4E00-\\u9FA5]+[([\\u4E00-\\u9FA5]+)]*</title>";
            Vector<String> v = GetMatch(w, rex);
            String name = "", c = "";
            int pos = 0;
            if (v.size() > 0) {
                c = v.elementAt(0);
                for (int j = 0; j < c.length(); ++j) {
                    if (c.charAt(j) == '>') {
                        pos = j;
                        break;
                    }
                }
                pos++;
                while (c.charAt(pos) != '<') {
                    name = name + c.charAt(pos);
                    pos++;
                }
                A[i].InsertName(name);
            }
            rex = "生日</th><td class=\"r\">[0-9]*-[0-9]*-[0-9]*";
            v.clear();
            v = GetMatch(w, rex);
            if (v.size() > 0) {
                c = v.elementAt(0);
                String birth = "";
                pos = 0;
                for (int j = 0; j < c.length(); ++j) {
                    if (c.charAt(j) >= '0' && c.charAt(j) <= '9') {
                        pos = j;
                        break;
                    }
                }
                while (pos < c.length()) {
                    birth = birth + c.charAt(pos);
                    pos++;
                }
                A[i].InsertBirth(birth);
            }
        }
    }

    //    no use
    public static void GetJson(int l, int r) throws IOException {
        String s1 = "AllJsonData\\";
        String s2 = ".json";
        String u1 = "https://www.goratings.org/players-json/data-";
        String u2 = ".json";
        for (int i = l; i <= r; ++i) {
            System.out.print(i + ": ");
            String u = u1 + Integer.toString(i) + u2;
            String s = s1 + Integer.toString(i) + s2;
            String w = Gget(u);
            File file = new File(s);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter fout = new BufferedWriter(fw);
            fout.write(w);
            fout.flush();
        }
    }

    public static int GetStrFirstNum(String s) {
        int t = 0, pos = -1;
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) >= '0' && s.charAt(i) <= '9') {
                pos = i;
                break;
            }
        }
        if (pos == -1) return -1;
        for (int i = pos; i < s.length() && s.charAt(i) >= '0' && s.charAt(i) <= '9'; ++i) {
            t *= 10;
            t += s.charAt(i) - '0';
        }
        return t;
    }

    public static void GetSexCountry(int l, int r) throws IOException {
        String s1 = "https://www.goratings.org/zh/history/", s2 = "-01-01.html";
        for (int sce = l; sce <= r; ++sce) {
            String w = Gget(s1 + Integer.toString(sce) + s2);
            String rex = "</tr><tr><td class=\"r\">.*</td>\n";
            Vector<String> ans = GetMatch(w, rex);
            for (String i : ans) {
                String id = GetMatch(i, "</td><td><a href=\"../../zh/players/[0-9]*.html\">").elementAt(0);
                int t = GetStrFirstNum(id);
                String sex = GetMatch(i, "<span style=\"color:.*\">.*</span>").elementAt(0);
                char sexv;
                if (GetMatch(sex, "♂").size() == 0) sexv = '♀';
                else sexv = '♂';
                A[t].InsertSex(sexv);
                String country = GetMatch(i, "<img src=\"/flags/.*.svg\" style=\"height:1em;vertical-align:middle\"").elementAt(0);
                int ct = 0;
                if (GetMatch(country, "jp").size() != 0) ct = 2;
                else if (GetMatch(country, "kr").size() != 0) ct = 3;
                else if (GetMatch(country, "cn").size() != 0) ct = 1;
                A[t].InsertCountry(ct);
//                System.out.println(t + " " + sexv + " " + ct);
            }
//            System.out.println(w);
        }
    }

    public static void main(String[] args) throws IOException {

        A = new Person[3010];
        for (int i = 0; i < 3010; ++i) A[i] = new Person();
        String s1 = "D:\\java_workplace\\web\\Go\\T2\\AllJsonData\\", s2 = ".json";
        for (int i = 0; i <= 3; ++i) {
            String s = s1 + Integer.toString(i) + s2;
            File file = new File(s);
            InputStream fin = new FileInputStream(file);
            byte[] buff = new byte[1024];
            String w = "";
            while (fin.read(buff) > 0) {
                w = w + new String(buff);
                buff = new byte[1024];
            }
            System.out.println(w);
        }
/*//        GetSexCountry(1980, 2020);
        GetPlayersNameBirth(1, 2223);
        File file = new File("D:\\java_workplace\\web\\Go\\T2\\Player1.csv");
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter fout = new BufferedWriter(fw);
        fout.write("id, name, birth\n");
        for (int i = 1; i <= 2223; ++i) {
            A[i].id = i;
            fout.write(A[i].id + " , " + A[i].name + " , " + A[i].birth + "\n");
            fout.flush();
        }

 */
    }
}