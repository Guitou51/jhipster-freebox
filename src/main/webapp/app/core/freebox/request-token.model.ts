export interface TokenRequest {
  appId: String;
  appName: String;
  appVersion: String;
  deviceName: String;
}

export interface TrackAuthorizeReponseData {
  status: AuthorizationStatus;
  challenge: String;
  passwordSalt: String;
}

export enum AuthorizationStatus {
  unknown,
  pending,
  timeout,
  granted,
  denied,
}
