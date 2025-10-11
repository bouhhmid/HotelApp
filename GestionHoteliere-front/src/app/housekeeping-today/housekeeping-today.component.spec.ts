import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HousekeepingTodayComponent } from './housekeeping-today.component';

describe('HousekeepingTodayComponent', () => {
  let component: HousekeepingTodayComponent;
  let fixture: ComponentFixture<HousekeepingTodayComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [HousekeepingTodayComponent]
    });
    fixture = TestBed.createComponent(HousekeepingTodayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
