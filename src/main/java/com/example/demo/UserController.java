package com.example.demo;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletConfig;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cglib.core.TypeUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@EnableAutoConfiguration
public class UserController {
	static final String DB_URL = "jdbc:postgresql://ec2-54-195-246-55.eu-west-1.compute.amazonaws.com:5432/d9071i6sf7pkb1";
    static final String USER = "cljdqecznltued";
    static final String PASS = "bc0b0900d8adf4b299f6077718c49a31b3bfd2fd37e836a70394f4d8fe5f5126";
    
    @RequestMapping(path="/user_auth/{email}/{pass}/")
    public Integer userAuth(@PathVariable("email") String email, @PathVariable("pass") String pass) {
    	System.out.print("userauth ");
    	int ID = -1;
		try {
			Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT \"ID\" FROM public.\"Users\" WHERE email='"+email+"' AND pass='"+pass+"'");
			if (rs.next()) {
				ID = rs.getInt("ID");
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done");
		System.out.println("");
    	return ID;
    }
    
    @RequestMapping(path="/mainpage/{ID}/")
    public User getMainPageContent(@PathVariable("ID") int ID) {
    	System.out.print("mainpage ");
    	String profileName = null;
    	String city = null;
    	Integer[] friendsIDs = null;
    	String[] friendsNames = null;
    	int notifications = 0;
    	boolean[] musicPreferences = new boolean[12];
    	try {
			Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM public.\"Users\" WHERE \"ID\"="+ID);
			rs.next();
			
			profileName = rs.getString("name");
			city = rs.getString("city");
			
			Array arr = rs.getArray(3);
			Integer[] music = (Integer[])arr.getArray();
			for (int i = 0; i < music.length; ++i) {
				if (music[i] == 1) {
					musicPreferences[i] = true;
				} else {
					musicPreferences[i] = false;
				}
			}
			
			arr = rs.getArray(7);
			if (arr != null) {
				friendsIDs = (Integer[])arr.getArray();
				friendsNames = new String[friendsIDs.length];
				for (int i = 0; i < friendsIDs.length; ++i) {
					ResultSet rs_ = stmt.executeQuery("SELECT name FROM public.\"Users\" WHERE \"ID\"="+friendsIDs[i]);
					if (rs_.next()) {
						friendsNames[i] = rs_.getString("name");
					}
					
					
				}
			}
			if (!rs.isClosed()) {
				arr = rs.getArray(8);
				if (arr != null) {
					notifications = ((Integer[])arr.getArray()).length;
				}
			}
			
			rs.close();
			stmt.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("done");
		System.out.println(ID+" "+profileName);
		System.out.println("");
        return new User(ID, profileName, musicPreferences, city, friendsIDs, friendsNames, notifications);
    }
    
    @RequestMapping(path="/user_reg/{name}/{email}/{pass}/{city}/")
    public Integer userReg(@PathVariable("name") String name, @PathVariable("email") String email, @PathVariable("pass") String pass, @PathVariable("city") String city) {
    	System.out.print("userreg ");
    	int ID = -1;
		try {
			Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT MAX(\"ID\") FROM public.\"Users\"");
			rs.next();
			ID = rs.getInt(1)+1;
			System.out.print(ID+" ");
			stmt.execute("INSERT INTO public.\"Users\"(\"ID\", name, email, pass, city) VALUES ("+ID+", '"+name+"', '"+email+"', '"+pass+"', '"+city+"');");
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("done");
		System.out.println("");
    	return ID;
    }
    
    
    @RequestMapping(path="/searchpage/{ID}/{count}/{params}/")
    public User[] getSearchPageContent(@PathVariable("ID") int ID, @PathVariable("count") int count, @PathVariable("params") String params) {
    	User[] userList_ = null;
    	if (params.equals("null")) {
    		System.out.print("searchpage ");
        	try {
        		User[] userList = new User[count];
        		Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
        		Statement stmt = con.createStatement();
            	for (int i = 0; i < count; ++i) {
            		ResultSet rs = stmt.executeQuery("SELECT \"ID\", name, \"musicPreferences\", city FROM public.\"Users\" WHERE \"ID\"="+i);
            		if (rs.next()) {
            			String profileName = rs.getString("name");
            			String city = rs.getString("city");
                		
            			boolean[] musicPreferences = new boolean[12];
            			Array arr = rs.getArray(3);
            			Integer[] music = (Integer[])arr.getArray();
            			for (int j = 0; j < music.length; ++j) {
            				if (music[j] == 1) {
            					musicPreferences[j] = true;
            				} else {
            					musicPreferences[j] = false;
            				}
            			}
            			User user = new User(i, profileName, musicPreferences, city, null, null, 0);
            			userList[i] = user;
            		} else {
            			break;
            		}
            		
            	}
            	stmt.close();
            	con.close();
            	int i = 0;
            	while (i < userList.length && userList[i] != null) {
            		i++;
            	}
            	userList_ = new User[i];
            	for (int j = 0; j < i; ++j) {
            		userList_[j] = userList[j];
            	}
        	} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	System.out.println("done");
    		System.out.println("");
        	return userList_;
    	} else {
    		User[] userList = new User[count];
    		Connection con;
			try {
				con = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = con.createStatement();
				for (int i = 0; i < count; ++i) {
					ResultSet rs = stmt.executeQuery("SELECT \"ID\", name, \"musicPreferences\", city FROM public.\"Users\" WHERE \"ID\"="+i);
					if (rs.next()) {
						String profileName = rs.getString("name");
						String profileNamelc = profileName.toLowerCase().replaceAll("\\s+","");
						String paramslc = params.toLowerCase().replaceAll("\\s+","");
						if (profileNamelc.contains(paramslc)) {
							String city = rs.getString("city");
		        			boolean[] musicPreferences = new boolean[12];
		        			Array arr = rs.getArray(3);
		        			Integer[] music = (Integer[])arr.getArray();
		        			for (int j = 0; j < music.length; ++j) {
		        				if (music[j] == 1) {
		        					musicPreferences[j] = true;
		        				} else {
		        					musicPreferences[j] = false;
		        				}
		        			}
		        			User user = new User(i, profileName, musicPreferences, city, null, null, 0);
		        			userList[i] = user;
						}
					} else {
						break;
					}
					
				}
				stmt.close();
            	con.close();
            	int i = 0;
            	while (i < userList.length && userList[i] != null) {
            		i++;
            	}
            	userList_ = new User[i];
            	for (int j = 0; j < i; ++j) {
            		userList_[j] = userList[j];
            	}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return userList_;
    	}
    }
    
    @RequestMapping("/avatars/{ID}/")
    public ResponseEntity<InputStreamResource> downloadAvatar(@PathVariable("ID") int ID) throws Exception {
    	System.out.print("download_avatar ");
    	File avatar = new File("C:\\avatars\\"+ID+"\\avatar.jpg");
    	if (avatar.exists()) {
    		InputStreamResource resource = new InputStreamResource(new FileInputStream(avatar));
            System.out.println("done");
    		System.out.println("");
            return ResponseEntity.ok()
                    // Content-Disposition
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + avatar.getName())
                    // Content-Type
                    .contentType(MediaType.valueOf("image/jpeg"))
                    // Content-Length
                    .contentLength(avatar.length()) //
                    .body(resource);
    	}
        return ResponseEntity.badRequest()
        		.body(null);
    }
    
    @RequestMapping(path="/friends_req_add/{myID}/{ID}/")
    public Boolean friendsReqAdd(@PathVariable("myID") int myID, @PathVariable("ID") int ID) {
    	System.out.print("friends_req_add ");
    	boolean inReq = false;
    	try {
			Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT \"friendsReq\" FROM public.\"Users\" WHERE \"ID\"="+myID);
			rs.next();
			Array arr = rs.getArray(1);
			
			ArrayList<Integer> alreq = new ArrayList<>();
			if (arr != null) {
				Integer[] req = (Integer[])arr.getArray();
				for (int elem:req) {
					if (elem == ID) {
						inReq = true;
					} else {
						alreq.add(elem);
					}
				}
			}
			
			Integer[] newintarr = new Integer[alreq.size()];
			for(int i = 0; i < alreq.size(); ++i) {
				newintarr[i] = alreq.get(i);
			}
			Array newarr = con.createArrayOf("integer", newintarr);
			String sql = "UPDATE public.\"Users\" SET \"friendsReq\"=? WHERE \"ID\"="+myID;
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setArray(1, newarr);
			pstmt.executeUpdate();
			pstmt.close();
			rs.close();			
			if(!inReq) {
				rs = stmt.executeQuery("SELECT \"friendsReq\" FROM public.\"Users\" WHERE \"ID\"="+ID);
				rs.next();
				Array prevarr = rs.getArray(1);
				Integer[] newintarr1 = new Integer[1];
				if (prevarr != null) {
					Integer[] previntarr = (Integer[])prevarr.getArray();
					newintarr1 = new Integer[previntarr.length+1];
					for (int i = 0; i < previntarr.length; ++i) {
						newintarr1[i] = previntarr[i];
					}
					newintarr1[previntarr.length] = myID;
				}
				Array newarr1 = con.createArrayOf("integer", newintarr1);
				String sql1 = "UPDATE public.\"Users\" SET \"friendsReq\"=? WHERE \"ID\"="+ID;
				PreparedStatement ps = con.prepareStatement(sql1);
				ps.setArray(1, newarr1);
				ps.executeUpdate();
				ps.close();
				rs.close();
			} else {
				rs = stmt.executeQuery("SELECT friends FROM public.\"Users\" WHERE \"ID\"="+ID);
				rs.next();
				Array prevarr = rs.getArray(1);
				Integer[] newintarr1 = new Integer[1];
				if (prevarr != null) {
					Integer[] previntarr = (Integer[])prevarr.getArray();
					newintarr1 = new Integer[previntarr.length+1];
					for (int i = 0; i < previntarr.length; ++i) {
						newintarr1[i] = previntarr[i];
					}
					newintarr1[previntarr.length] = myID;
				} else {
					newintarr1[0] = myID;
				}
				Array newarr1 = con.createArrayOf("integer", newintarr1);
				String sql1 = "UPDATE public.\"Users\" SET friends=? WHERE \"ID\"="+ID;
				PreparedStatement ps = con.prepareStatement(sql1);
				ps.setArray(1, newarr1);
				ps.executeUpdate();
				ps.close();
				rs.close();
				
				rs = stmt.executeQuery("SELECT friends FROM public.\"Users\" WHERE \"ID\"="+myID);
				rs.next();
				prevarr = rs.getArray(1);
				newintarr1 = new Integer[1];
				if (prevarr != null) {
					Integer[] previntarr = (Integer[])prevarr.getArray();
					newintarr1 = new Integer[previntarr.length+1];
					for (int i = 0; i < previntarr.length; ++i) {
						newintarr1[i] = previntarr[i];
					}
					newintarr1[previntarr.length] = ID;
				} else {
					newintarr1[0] = ID;
				}
				newarr1 = con.createArrayOf("integer", newintarr1);
				sql1 = "UPDATE public.\"Users\" SET friends=? WHERE \"ID\"="+myID;
				ps = con.prepareStatement(sql1);
				ps.setArray(1, newarr1);
				ps.executeUpdate();
				ps.close();
				rs.close();
			}
			stmt.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("done");
		System.out.println("");
		return inReq;
    	
    }
    
    @RequestMapping(path="/friends_del/{myID}/{ID}/")
    public Boolean friendsDel(@PathVariable("myID") int myID, @PathVariable("ID") int ID) {
    	System.out.print("friends_del ");
    	boolean ans = false;
    	try {
			Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT friends FROM public.\"Users\" WHERE \"ID\"="+myID);
			rs.next();
			Array arr = rs.getArray(1);
			Integer[] intarr = (Integer[]) arr.getArray();
			if (intarr.length == 1) {
				stmt.execute("UPDATE public.\"Users\" SET friends='{}' WHERE \"ID\"="+myID);
				rs.close();
			} else {
				Integer[] newintarr;
				if (intarr.length-1 < 1) {
					newintarr = new Integer[1];
				} else {
					newintarr = new Integer[intarr.length-1];
				}
				boolean found = false;
				for (int i = 0; i < intarr.length; ++i) {
					if (intarr[i] == ID) {
						found = true;
					} else {
						if(found) {
							newintarr[i-1] = intarr[i];
						} else {
							newintarr[i] = intarr[i];
						}
					}
				}
				arr = con.createArrayOf("integer", newintarr);
				String sql = "UPDATE public.\"Users\" SET friends=? WHERE \"ID\"="+myID; 
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setArray(1, arr);
				ps.executeUpdate();
				ps.close();
			}
			rs.close();
			
			rs = stmt.executeQuery("SELECT friends FROM public.\"Users\" WHERE \"ID\"="+ID);
			rs.next();
			arr = rs.getArray(1);
			intarr = (Integer[]) arr.getArray();
			if (intarr.length == 1) {
				stmt.execute("UPDATE public.\"Users\" SET friends='{}' WHERE \"ID\"="+ID);
				rs.close();
			} else {
				Integer[] newintarr;
				if (intarr.length-1 < 1) {
					newintarr = new Integer[1];
				} else {
					newintarr = new Integer[intarr.length-1];
				}
				boolean found = false;
				for (int i = 0; i < intarr.length; ++i) {
					if (intarr[i] == myID) {
						found = true;
					} else {
						if(found) {
							newintarr[i-1] = intarr[i];
						} else {
							newintarr[i] = intarr[i];
						}
					}
				}
				arr = con.createArrayOf("integer", newintarr);
				String sql = "UPDATE public.\"Users\" SET friends=? WHERE \"ID\"="+ID; 
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setArray(1, arr);
				ps.executeUpdate();
				ps.close();
			}
			rs.close();
			stmt.close();
			con.close();
	    	System.out.println("done");
			System.out.println("");
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ans;
    }
    
    @RequestMapping(path="/get_status/{myID}/{ID}/")
    public Integer getStatus(@PathVariable("myID") int myID, @PathVariable("ID") int ID) {
    	Integer ans = -1;
    	System.out.print("get_status ");
    	try {
			Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT \"friendsReq\" FROM public.\"Users\" WHERE \"ID\"="+myID);
			rs.next();
			Array arr = rs.getArray(1);
			if (arr != null) {
				Integer[] intarr = (Integer[]) arr.getArray();
				for (int i = 0; i < intarr.length; ++i) {
					if (intarr[i] == ID) {
						rs.close();
						stmt.close();
						con.close();
						ans = 1;
						System.out.println(ans);
						return ans;
					}
				}
			}
			rs.close();
			
			rs = stmt.executeQuery("SELECT \"friendsReq\" FROM public.\"Users\" WHERE \"ID\"="+ID);
			rs.next();
			arr = rs.getArray(1);
			if (arr != null) {
				Integer[] intarr = (Integer[]) arr.getArray();
				for (int i = 0; i < intarr.length; ++i) {
					if (intarr[i] == myID) {
						rs.close();
						stmt.close();
						con.close();
						ans = 2;
						System.out.println(ans);
						return ans;
					}
				}
			}
			rs.close();
			
			rs = stmt.executeQuery("SELECT friends FROM public.\"Users\" WHERE \"ID\"="+ID);
			rs.next();
			arr = rs.getArray(1);
			if (arr != null) {
				Integer[] intarr = (Integer[]) arr.getArray();
				for (int i = 0; i < intarr.length; ++i) {
					if (intarr[i] == myID) {
						rs.close();
						stmt.close();
						con.close();
						ans = 3;
						System.out.println(ans);
						return ans;
					}
				}
			}
			rs.close();
			stmt.close();
			con.close();
			ans = 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println(ans);
    	System.out.println("done");
		System.out.println("");
		return ans;
    }
    
    @RequestMapping(path="/set_music_prefs")
    @ResponseBody public String setMusicPrefs(@RequestParam("ID") int ID, @RequestParam("music_prefs") boolean[] musicPrefs) {
    	System.out.print("set_music_prefs ");
    	
    	try {
    		Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
    		Integer[] musicPreferences = new Integer[musicPrefs.length];
    		for (int i = 0; i < musicPrefs.length; ++i) {
    			if (musicPrefs[i]) {
    				musicPreferences[i] = 1;
    			} else {
    				musicPreferences[i] = 0;
    			}
    		}
    		Array arr = con.createArrayOf("integer", musicPreferences);
    		String sql = "UPDATE public.\"Users\" SET \"musicPreferences\"=? WHERE \"ID\"="+ID;
    		PreparedStatement pstmt = con.prepareStatement(sql);
    		pstmt.setArray(1, arr);
    		pstmt.executeUpdate();
    		
    		pstmt.close();
    		con.close();
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("done");
		System.out.println("");
    	return "";
    }
    
    @RequestMapping(path="/set_avatar/{ID}/")
    @ResponseBody public String setAvatar(@RequestPart("file") MultipartFile file, @PathVariable("ID") int ID) {
    	System.out.print("set_avatar ");
    	File dir = new File("C:\\avatars\\"+String.valueOf(ID)+"\\");
    	dir.mkdir();
    	File f = new File("C:\\avatars\\"+String.valueOf(ID)+"\\avatar.jpg");
    	try {
			file.transferTo(f);
			new FileSystemResource(f);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("done");
		System.out.println("");
    	return "";
    }
}
