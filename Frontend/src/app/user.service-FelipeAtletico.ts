import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  // This is the url of the web api that we will use to send the data to the server.
  private apiUrl = "https://alarcosj.esi.uclm.es/fakeAccountsBE/users"
  private token: string | null = null;
  constructor(private http: HttpClient) { }

  checkCookie() {
    return this.http.get(this.apiUrl + "/checkCookie", 
		{ responseType : "text", withCredentials : true })
  }

  
  register1(email:string, pw1:string, pw2:string){
    let infoJSN = {email:email, pwd1:pw1, pwd2:pw2}

    let urlRegister1=this.apiUrl+"/registrar1"
    return this.http.post<any>(urlRegister1, infoJSN)
  }

  login1(email : string, pwd : string) {
    let info = {
      email : email,
      pwd : pwd
    }
    let urlIniciarSesion1=this.apiUrl+'/login1';
    return this.http.put<any>(urlIniciarSesion1, info,  
      { responseType: 'text' as 'json', withCredentials : true}).pipe()
  
  }

}
