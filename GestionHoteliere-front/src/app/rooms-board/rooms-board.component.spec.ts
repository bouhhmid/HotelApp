import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomsBoardComponent } from './rooms-board.component';

describe('RoomsBoardComponent', () => {
  let component: RoomsBoardComponent;
  let fixture: ComponentFixture<RoomsBoardComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RoomsBoardComponent]
    });
    fixture = TestBed.createComponent(RoomsBoardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
