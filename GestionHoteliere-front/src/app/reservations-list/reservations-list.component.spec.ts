import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReservationsListComponent } from './reservations-list.component';

describe('ReservationsListComponent', () => {
  let component: ReservationsListComponent;
  let fixture: ComponentFixture<ReservationsListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ReservationsListComponent]
    });
    fixture = TestBed.createComponent(ReservationsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
