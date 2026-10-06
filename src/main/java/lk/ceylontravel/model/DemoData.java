package lk.ceylontravel.model;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lk.ceylontravel.repository.Store;

@Component
@ConditionalOnProperty(name="app.demo-data",havingValue="true")
public class DemoData implements CommandLineRunner {

    private final Store db;
    private final PasswordEncoder encoder;

    public DemoData(Store db,PasswordEncoder encoder) {
        this.db=db;this.encoder=encoder;
    }

    @Override 
    @Transactional 
    public void run(String... args) {
        var userCount = db.list("SELECT id FROM users");
        if(!userCount.isEmpty())
            return;

        String password=encoder.encode("Ceylon123!");
        long admin=user("Ceylon Admin","admin@ceylon.test","ADMIN",password);
        long tourist=user("Alex Morgan","tourist@ceylon.test","TOURIST",password);
        long community=user("Nethmi Perera","community@ceylon.test","COMMUNITY",password);
        long guide=user("Kasun Perera","guide@ceylon.test","GUIDE",password);
        long guide2=user("Dilini Fernando","dilini@ceylon.test","GUIDE",password);
        long guide3=user("Ravindu Silva","ravindu@ceylon.test","GUIDE",password);

        profile(guide,"A local storyteller with a love for the cultural triangle. Join me for ancient cities, village walks, and a fresh perspective on Sri Lankan heritage.","Sigiriya & Kandy","English, Sinhala",8500,"sigiriya");
        profile(guide2,"Coastal walks, hidden courtyards, and the stories behind Galle Fort. My tours leave plenty of time for local food and a pause by the ocean.","Galle & South Coast","English, Sinhala, Tamil",7500,"galle");
        profile(guide3,"Explore the hill country at your own pace. I lead walks through Ella, tea-growing villages, and the green trails around the Nine Arch Bridge.","Ella & Hill Country","English, Sinhala",9000,"ella");
        long sigiriya=place(community,admin,"Sigiriya Rock Fortress","An ancient rock fortress rising above the central plains. Explore the gardens, climb the stone stairways, and take in the wide views with a local guide.","Sigiriya, Central Province","Heritage","Plan an early start, carry water, and wear comfortable walking shoes.","sigiriya");
        long galle=place(community,admin,"Galle Fort","Seaside ramparts, quiet lanes, and layers of coastal history. Walk through the old town and discover its lighthouse, courtyards, and local shops.","Galle, Southern Province","Heritage","Late afternoon is a comfortable time for a walk along the ramparts.","galle");
        long ella=place(community,admin,"Nine Arch Bridge","A graceful stone railway bridge tucked into the green hills of Ella. Discover nearby walking trails and the landscape of Sri Lanka's tea country.","Ella, Uva Province","Nature","Stay clear of the railway tracks and follow local guidance.","ella");
        long mirissa=place(community,admin,"Mirissa Beach","A sweeping south-coast bay with sandy shores and ocean views. Set aside a slow day for a coastal walk and a taste of the local food.","Mirissa, Southern Province","Beach","Check local sea conditions before swimming.","mirissa");
        for(long id:new long[]{guide,guide2,guide3}) for(int d=0;d<60;d++) db.update("INSERT INTO availability(guide_id,available_date) VALUES(?,?)",id,LocalDate.now().plusDays(d));
        long completed=db.insert("INSERT INTO bookings(tourist_id,guide_id,place_id,start_date,end_date,guests,details,status,total_price) VALUES(?,?,?,?,?,?,?,?,?)",tourist,guide,sigiriya,LocalDate.now().minusDays(3),LocalDate.now().minusDays(3),2,"A relaxed heritage walk","COMPLETED",8500);
        db.update("INSERT INTO reviews(author_id,booking_id,rating,comment) VALUES(?,?,?,?)",tourist,completed,5,"A thoughtful day out. Kasun shared the stories behind the fortress and made time for every question.");

        for(long id:new long[]{sigiriya,galle,ella,mirissa}) {
            var p=db.one("SELECT * FROM places WHERE id=?",id);
            db.update("INSERT INTO gallery_images(user_id,place_id,image_url,caption) VALUES(?,?,?,?)",tourist,id,p.get("image_url"),"A day at "+p.get("name"));
        }
        db.update("INSERT INTO notifications(user_id,text) VALUES(?,?)",tourist,"Welcome to Ceylon Travel. Your next journey starts here.");
    }
    private long user(String name,String email,String role,String password) {
        return db.insert("INSERT INTO users(name,email,role,password_hash) VALUES(?,?,?,?)",name,email,role,password);
    }

    private void profile(long id,String bio,String location,String languages,int rate,String image) {
        db.update("INSERT INTO guide_profiles(user_id,bio,location,daily_rate,verified,image_url) VALUES(?,?,?,?,1,?)",id,bio,location,rate,"/images/"+image+".jpg");
        for(String language:languages.split(", "))
            db.update("INSERT INTO guide_languages(guide_id,language) VALUES(?,?)",id,language);
    }

    private long place(long owner,long admin,String name,String description,String location,String category,String tips,String image) {
        return db.insert("INSERT INTO places(submitted_by,name,description,location,category,tips,image_url,status,reviewed_by,reviewed_at) VALUES(?,?,?,?,?,?,?,'APPROVED',?,CURRENT_TIMESTAMP)",owner,name,description,location,category,tips,"/images/"+image+".jpg",admin);
    }
}
