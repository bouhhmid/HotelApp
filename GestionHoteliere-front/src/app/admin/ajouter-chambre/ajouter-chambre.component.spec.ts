import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AjouterChambreComponent } from './ajouter-chambre.component';

describe('AjouterChambreComponent', () => {
  let component: AjouterChambreComponent;
  let fixture: ComponentFixture<AjouterChambreComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AjouterChambreComponent]
    });
    fixture = TestBed.createComponent(AjouterChambreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
