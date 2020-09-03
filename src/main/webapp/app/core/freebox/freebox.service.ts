import { Injectable } from '@angular/core';
import { SERVER_API_URL } from '../../app.constants';
import { HttpClient, HttpDownloadProgressEvent, HttpEvent, HttpEventType, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TokenRequest, TrackAuthorizeReponseData } from './request-token.model';
import { LocalStorageService, SessionStorageService } from 'ngx-webstorage';
import { filter, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class FreeboxService {
  public resourceUrl: string = SERVER_API_URL + 'api/freebox';

  constructor(private localStorage: LocalStorageService, private sessionStorage: SessionStorageService, private http: HttpClient) {}

  postRequestAuthorization(request: TokenRequest): Observable<number> {
    return this.http.post<number>(`${this.resourceUrl}/login/authorize`, request);
  }
  getTrackId(): Observable<number> {
    return this.http.get<number>(`${this.resourceUrl}/login/authorize/track`);
  }

  getRequestAuthorization(): Observable<TokenRequest> {
    // return of({appId: '1', appName: 'JHispterFreebox', appVersion: '0.0.1', deviceName: 'JHispterFreebox'})
    return this.http.get<TokenRequest>(`${this.resourceUrl}/login/authorize`);
  }

  trackRequestAuthorization(trackId: number): Observable<TrackAuthorizeReponseData> {
    const httpHeaders: HttpHeaders = new HttpHeaders();
    // httpHeaders.append("Accept", "text/event-stream")
    return this.http
      .get(`${this.resourceUrl}/login/authorize/${trackId}`, {
        headers: httpHeaders,
        responseType: 'text',
        observe: 'events',
        reportProgress: true,
      })
      .pipe(
        filter((httpEvent: HttpEvent<string>) => httpEvent.type === HttpEventType.DownloadProgress),
        map((httpEvent: HttpEvent<string>) => httpEvent as HttpDownloadProgressEvent),
        filter(value => value.partialText !== undefined),
        map(value => value.partialText as string),
        map(value => value.replace(/data:/g, '')),
        map(value => {
          const lastIndex = value.lastIndexOf('{');
          return JSON.parse(value.substring(lastIndex));
        })
      );
  }

  loginChallengeValue(): Observable<any> {
    return this.http.get<any>(`${this.resourceUrl}/login`);
  }

  loginOpenSession(): Observable<any> {
    return this.http.post<any>(`${this.resourceUrl}/login/session`, null);
  }
}
