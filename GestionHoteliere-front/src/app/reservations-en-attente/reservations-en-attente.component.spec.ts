import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReservationsEnAttenteComponent } from './reservations-en-attente.component';

describe('ReservationsEnAttenteComponent', () => {
  let component: ReservationsEnAttenteComponent;
  let fixture: ComponentFixture<ReservationsEnAttenteComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ReservationsEnAttenteComponent]
    });
    fixture = TestBed.createComponent(ReservationsEnAttenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
