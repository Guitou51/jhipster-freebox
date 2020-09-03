import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { FreeboxService } from '../../../core/freebox/freebox.service';
import { EMPTY, Observable } from 'rxjs';
import { TokenRequest } from '../../../core/freebox/request-token.model';
import { switchMap, finalize, tap, filter, mergeMap } from 'rxjs/operators';

@Component({
  selector: 'jhi-freebox-config',
  templateUrl: './freebox-config.component.html',
  styleUrls: ['./freebox-config.component.scss'],
})
export class FreeboxConfigComponent implements OnInit {
  observable: Observable<any> = EMPTY;
  requestTokenForm = this.fb.group({
    appId: ['', Validators.required],
    appName: ['', Validators.required],
    appVersion: ['', Validators.required],
    deviceName: ['', Validators.required],
  });
  isSaving = false;

  constructor(private fb: FormBuilder, private freeboxService: FreeboxService) {
    freeboxService
      .getTrackId()
      .pipe(
        filter(value => value !== null),
        mergeMap(() =>
          freeboxService.getRequestAuthorization().pipe(
            tap(value => {
              if (value !== null) {
                this.requestTokenForm.controls['appId'].setValue(value.appId);
                this.requestTokenForm.controls['appName'].setValue(value.appName);
                this.requestTokenForm.controls['appVersion'].setValue(value.appVersion);
                this.requestTokenForm.controls['deviceName'].setValue(value.deviceName);
              }
            })
          )
        )
      )
      .subscribe();
  }

  ngOnInit(): void {}

  onSave(): void {
    this.isSaving = true;
    const requestToken = this.requestTokenForm.getRawValue() as TokenRequest;
    this.freeboxService
      .postRequestAuthorization(requestToken)
      .pipe(
        switchMap(trackId => this.freeboxService.trackRequestAuthorization(trackId)),
        finalize(() => (this.isSaving = false))
      )
      .subscribe();

    // this.observable = this.freeboxService.trackRequestAuthorization(1).pipe(
    //   // tap(x => x),
    //   concatMapTo(this.freeboxService.loginOpenSession())
    // );
  }
}
